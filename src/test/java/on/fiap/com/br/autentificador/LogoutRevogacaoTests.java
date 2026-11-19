package on.fiap.com.br.autentificador;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import on.fiap.com.br.autentificador.dto.SairRequisicao;
import on.fiap.com.br.autentificador.mensageria.AcessoRevogadoEvento;
import on.fiap.com.br.autentificador.models.Cuidador;
import on.fiap.com.br.autentificador.models.Parente;
import on.fiap.com.br.autentificador.models.TokenAtualizacao;
import on.fiap.com.br.autentificador.repositories.CuidadorRepository;
import on.fiap.com.br.autentificador.repositories.ParenteRepository;
import on.fiap.com.br.autentificador.repositories.TokenAtualizacaoRepository;
import on.fiap.com.br.autentificador.services.AutenticacaoService;
import on.fiap.com.br.autentificador.services.TokenJwtService;
import on.fiap.com.br.autentificador.services.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Logout: o evento de acesso revogado (enviado ao RabbitMQ depois do commit) só é publicado
 * quando o token de acesso pertence ao mesmo usuário do token de atualização.
 * Não sobe Spring nem containers.
 */
class LogoutRevogacaoTests {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-32-caracteres";

    private TokenAtualizacaoRepository tokenAtualizacaoRepository;
    private ApplicationEventPublisher publicador;
    private TokenJwtService tokenJwtService;
    private AutenticacaoService autenticacaoService;

    @BeforeEach
    void preparar() {
        SecretKeySpec chave = new SecretKeySpec(SEGREDO.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        tokenJwtService = new TokenJwtService(new NimbusJwtEncoder(new ImmutableSecret<>(chave)));
        ReflectionTestUtils.setField(tokenJwtService, "expiracaoMinutos", 60L);

        tokenAtualizacaoRepository = mock(TokenAtualizacaoRepository.class);
        publicador = mock(ApplicationEventPublisher.class);
        autenticacaoService = new AutenticacaoService(mock(CuidadorRepository.class), mock(ParenteRepository.class),
                tokenAtualizacaoRepository, mock(PasswordEncoder.class), tokenJwtService,
                NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build(), publicador);
    }

    @Test
    void logoutComTokenDoMesmoUsuarioPublicaEvento() {
        Cuidador maria = cuidador(1L);
        TokenAtualizacao refresh = refreshDe(maria, null);
        String tokenAcesso = tokenJwtService.gerarTokenAcesso(UsuarioAutenticado.de(maria));

        autenticacaoService.sair(new SairRequisicao("refresh", tokenAcesso));

        ArgumentCaptor<AcessoRevogadoEvento> evento = ArgumentCaptor.forClass(AcessoRevogadoEvento.class);
        verify(publicador).publishEvent(evento.capture());
        assertThat(evento.getValue().jti()).isNotBlank();
        assertThat(evento.getValue().idUsuario()).isEqualTo(1L);
        assertThat(evento.getValue().tipoUsuario()).isEqualTo("CUIDADOR");
        assertThat(evento.getValue().motivo()).isEqualTo("LOGOUT");
        assertThat(refresh.getDataRevogacao()).isNotNull();
    }

    @Test
    void parenteComMesmoIdDeUmCuidadorNaoRevogaOTokenDoCuidador() {
        Parente ana = new Parente();
        ana.setId(1L);
        refreshDe(null, ana);
        String tokenDoCuidador = tokenJwtService.gerarTokenAcesso(UsuarioAutenticado.de(cuidador(1L)));

        autenticacaoService.sair(new SairRequisicao("refresh", tokenDoCuidador));

        verify(publicador, never()).publishEvent(any(Object.class));
    }

    @Test
    void logoutSemTokenDeAcessoSoRevogaORefresh() {
        TokenAtualizacao refresh = refreshDe(cuidador(1L), null);

        autenticacaoService.sair(new SairRequisicao("refresh", null));

        verify(publicador, never()).publishEvent(any(Object.class));
        assertThat(refresh.getDataRevogacao()).isNotNull();
    }

    @Test
    void tokenDeAcessoInvalidoNaoPublicaEvento() {
        refreshDe(cuidador(1L), null);

        autenticacaoService.sair(new SairRequisicao("refresh", "isto-nao-e-um-jwt"));

        verify(publicador, never()).publishEvent(any(Object.class));
    }

    private Cuidador cuidador(Long id) {
        Cuidador cuidador = new Cuidador();
        cuidador.setId(id);
        cuidador.setNome("Maria");
        return cuidador;
    }

    private TokenAtualizacao refreshDe(Cuidador cuidador, Parente parente) {
        TokenAtualizacao token = new TokenAtualizacao();
        token.setCuidador(cuidador);
        token.setParente(parente);
        token.setDataExpiracao(LocalDateTime.now().plusDays(7));
        when(tokenAtualizacaoRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        return token;
    }
}
