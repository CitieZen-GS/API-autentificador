package on.fiap.com.br.autentificador.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import on.fiap.com.br.autentificador.dto.LoginRequisicao;
import on.fiap.com.br.autentificador.dto.SairRequisicao;
import on.fiap.com.br.autentificador.dto.TokenResposta;
import on.fiap.com.br.autentificador.dto.UsuarioResposta;
import on.fiap.com.br.autentificador.mensageria.AcessoRevogadoEvento;
import on.fiap.com.br.autentificador.models.Cuidador;
import on.fiap.com.br.autentificador.models.Parente;
import on.fiap.com.br.autentificador.models.TokenAtualizacao;
import on.fiap.com.br.autentificador.repositories.CuidadorRepository;
import on.fiap.com.br.autentificador.repositories.ParenteRepository;
import on.fiap.com.br.autentificador.repositories.TokenAtualizacaoRepository;
import on.fiap.com.br.autentificador.services.excecoes.AutenticacaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Login, renovação e saída.
 * <ul>
 *   <li>E-mail encontrado em Cuidador: token com role CUIDADOR (leitura e escrita).</li>
 *   <li>E-mail encontrado em Parente: token com role FAMILIAR (somente leitura) e a claim id_idoso.</li>
 * </ul>
 * O cuidador é verificado primeiro. Se o mesmo e-mail existir nas duas tabelas, vale a senha que confere.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";
    private static final String TOKEN_INVALIDO = "Token de atualização inválido ou expirado";

    private final CuidadorRepository cuidadorRepository;
    private final ParenteRepository parenteRepository;
    private final TokenAtualizacaoRepository tokenAtualizacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenJwtService tokenJwtService;
    private final JwtDecoder jwtDecoder;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final SecureRandom geradorAleatorio = new SecureRandom();

    @Value("${jwt.expiracao-atualizacao-dias}")
    private long expiracaoAtualizacaoDias;

    /** Hash fictício: quando o e-mail não existe, a comparação leva o mesmo tempo (não revela quem tem conta). */
    private String hashFicticio;

    @Transactional
    public TokenResposta login(LoginRequisicao requisicao) {
        String email = CadastroService.normalizarEmail(requisicao.email());
        String senha = requisicao.senha();

        Optional<Cuidador> cuidador = cuidadorRepository.findByEmail(email);
        if (cuidador.isPresent() && senhaConfere(senha, cuidador.get().getSenha())) {
            return emitirTokens(UsuarioAutenticado.de(cuidador.get()), cuidador.get(), null);
        }

        Optional<Parente> parente = parenteRepository.findByEmail(email);
        if (parente.isPresent() && senhaConfere(senha, parente.get().getSenha())) {
            return emitirTokens(UsuarioAutenticado.de(parente.get()), null, parente.get());
        }

        if (cuidador.isEmpty() && parente.isEmpty()) {
            passwordEncoder.matches(senha, hashFicticio());
        }
        throw new AutenticacaoException(CREDENCIAIS_INVALIDAS);
    }

    /** Troca um token de atualização válido por um novo par de tokens. O token usado é revogado (rotação). */
    @Transactional
    public TokenResposta atualizar(String tokenAtualizacao) {
        TokenAtualizacao token = buscarTokenValido(tokenAtualizacao);
        token.setDataRevogacao(LocalDateTime.now());

        if (token.getCuidador() != null) {
            return emitirTokens(UsuarioAutenticado.de(token.getCuidador()), token.getCuidador(), null);
        }
        return emitirTokens(UsuarioAutenticado.de(token.getParente()), null, token.getParente());
    }

    /**
     * Logout: revoga o token de atualização e, se o token de acesso vier junto e for do mesmo usuário,
     * avisa as outras APIs (RabbitMQ) para recusá-lo na hora, sem esperar ele expirar.
     */
    @Transactional
    public void sair(SairRequisicao requisicao) {
        Optional<TokenAtualizacao> tokenAtualizacao =
                tokenAtualizacaoRepository.findByTokenHash(gerarHash(requisicao.tokenAtualizacao()));
        if (tokenAtualizacao.isEmpty()) {
            return;
        }
        TokenAtualizacao token = tokenAtualizacao.get();
        if (token.getDataRevogacao() == null) {
            token.setDataRevogacao(LocalDateTime.now());
        }
        if (requisicao.tokenAcesso() != null && !requisicao.tokenAcesso().isBlank()) {
            revogarTokenAcesso(requisicao.tokenAcesso(), token);
        }
    }

    /** O evento só sai depois do commit (ver PublicadorEventos). */
    private void revogarTokenAcesso(String tokenAcesso, TokenAtualizacao dono) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(tokenAcesso);
        } catch (JwtException e) {
            // Expirado ou inválido: já é recusado pelas outras APIs, não há o que revogar
            return;
        }
        if (jwt.getId() == null || !pertenceAoMesmoUsuario(jwt, dono)) {
            log.warn("Logout com token de acesso de outro usuário ou sem jti: ignorado");
            return;
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        publicadorDeEventos.publishEvent(new AcessoRevogadoEvento(jwt.getId(), Long.valueOf(jwt.getSubject()),
                roles.getFirst(), jwt.getExpiresAt(), Instant.now(), "LOGOUT"));
    }

    private boolean pertenceAoMesmoUsuario(Jwt jwt, TokenAtualizacao dono) {
        UsuarioAutenticado usuarioDoToken = UsuarioAutenticado.doToken(jwt);
        UsuarioAutenticado usuarioDoLogout = dono.getCuidador() != null
                ? UsuarioAutenticado.de(dono.getCuidador())
                : UsuarioAutenticado.de(dono.getParente());
        return usuarioDoToken.tipo() == usuarioDoLogout.tipo() && usuarioDoToken.id().equals(usuarioDoLogout.id());
    }

    /** Usuário do token já validado pelo Spring Security (rota GET /autenticacao/eu). */
    public UsuarioResposta usuarioDoToken(Jwt jwt) {
        return UsuarioResposta.de(UsuarioAutenticado.doToken(jwt));
    }

    /** Login automático logo após o cadastro do cuidador. Roda na transação de quem chamou. */
    @Transactional
    public TokenResposta emitirTokens(Cuidador cuidador) {
        return emitirTokens(UsuarioAutenticado.de(cuidador), cuidador, null);
    }

    private TokenResposta emitirTokens(UsuarioAutenticado usuario, Cuidador cuidador, Parente parente) {
        String tokenAtualizacao = gerarTokenAleatorio();

        TokenAtualizacao registro = new TokenAtualizacao();
        registro.setTokenHash(gerarHash(tokenAtualizacao));
        registro.setCuidador(cuidador);
        registro.setParente(parente);
        registro.setDataExpiracao(LocalDateTime.now().plusDays(expiracaoAtualizacaoDias));
        tokenAtualizacaoRepository.save(registro);

        return new TokenResposta(tokenJwtService.gerarTokenAcesso(usuario), "Bearer",
                tokenJwtService.getExpiracaoSegundos(), tokenAtualizacao, UsuarioResposta.de(usuario));
    }

    private TokenAtualizacao buscarTokenValido(String tokenAtualizacao) {
        TokenAtualizacao token = tokenAtualizacaoRepository.findByTokenHash(gerarHash(tokenAtualizacao))
                .orElseThrow(() -> new AutenticacaoException(TOKEN_INVALIDO));
        if (token.getDataRevogacao() != null || token.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new AutenticacaoException(TOKEN_INVALIDO);
        }
        return token;
    }

    private boolean senhaConfere(String senha, String hashSalvo) {
        return hashSalvo != null && !hashSalvo.isBlank() && passwordEncoder.matches(senha, hashSalvo);
    }

    private String hashFicticio() {
        if (hashFicticio == null) {
            hashFicticio = passwordEncoder.encode("senha-ficticia-para-tempo-constante");
        }
        return hashFicticio;
    }

    /** 32 bytes aleatórios em Base64 URL (43 caracteres). */
    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        geradorAleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 em hexadecimal: é o que fica salvo em ds_token_hash. */
    private String gerarHash(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
