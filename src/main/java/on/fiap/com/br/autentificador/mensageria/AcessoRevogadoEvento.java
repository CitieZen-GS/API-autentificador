package on.fiap.com.br.autentificador.mensageria;

import java.time.Instant;

/**
 * Mensagem publicada quando um token de acesso deixa de valer antes de expirar (ex.: logout).
 * As outras APIs passam a recusar o token com este jti até {@code expiraEm}.
 * <p>
 * Contrato entre as APIs: a API de funções tem um record igual. Mudou um campo? Mude nos dois lados
 * e no CLAUDE.md.
 *
 * @param jti         id único do JWT (claim "jti")
 * @param idUsuario   claim "sub": id_cuidador ou id_parente
 * @param tipoUsuario CUIDADOR ou FAMILIAR
 * @param expiraEm    claim "exp": depois disso o token já seria recusado de qualquer forma
 * @param revogadoEm  quando o acesso foi revogado
 * @param motivo      por enquanto só LOGOUT
 */
public record AcessoRevogadoEvento(String jti, Long idUsuario, String tipoUsuario, Instant expiraEm,
                                   Instant revogadoEm, String motivo) {
}
