package on.fiap.com.br.autentificador.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Gera o token de acesso (JWT HS256) no formato que a API de funções valida.
 * Contrato das claims descrito no CLAUDE.md.
 */
@Service
@RequiredArgsConstructor
public class TokenJwtService {

    public static final String EMISSOR = "ajudante-familiar-autenticacao";

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiracao-acesso-minutos}")
    private long expiracaoMinutos;

    public String gerarTokenAcesso(UsuarioAutenticado usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .subject(String.valueOf(usuario.id()))
                .claim("roles", List.of(usuario.tipo().name()))
                .claim("nome", usuario.nome())
                .claim("permissoes", usuario.tipo().getPermissoes());

        if (usuario.email() != null) {
            claims.claim("email", usuario.email());
        }
        // Só o familiar fica preso a um idoso: a API de funções pode usar esta claim para filtrar
        if (usuario.idIdoso() != null) {
            claims.claim("id_idoso", usuario.idIdoso());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
    }

    /** Validade do token de acesso em segundos (campo expiraEm da resposta). */
    public long getExpiracaoSegundos() {
        return expiracaoMinutos * 60;
    }
}
