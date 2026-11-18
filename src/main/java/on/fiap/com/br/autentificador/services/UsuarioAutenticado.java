package on.fiap.com.br.autentificador.services;

import on.fiap.com.br.autentificador.models.Cuidador;
import on.fiap.com.br.autentificador.models.Parente;
import on.fiap.com.br.autentificador.models.enums.TipoUsuario;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Dados do usuário que entram no JWT, independentemente da tabela de origem.
 *
 * @param id      id_cuidador ou id_parente, conforme o tipo
 * @param idIdoso só para FAMILIAR: o idoso que o parente pode visualizar (null para CUIDADOR)
 */
public record UsuarioAutenticado(Long id, String nome, String email, TipoUsuario tipo, Long idIdoso) {

    public static UsuarioAutenticado de(Cuidador cuidador) {
        return new UsuarioAutenticado(cuidador.getId(), cuidador.getNome(), cuidador.getEmail(),
                TipoUsuario.CUIDADOR, null);
    }

    public static UsuarioAutenticado de(Parente parente) {
        return new UsuarioAutenticado(parente.getId(), parente.getNome(), parente.getEmail(),
                TipoUsuario.FAMILIAR, parente.getIdIdoso());
    }

    /** Remonta o usuário a partir das claims de um JWT já validado (contrato no CLAUDE.md). */
    public static UsuarioAutenticado doToken(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        TipoUsuario tipo = TipoUsuario.valueOf(roles.getFirst());
        String idIdoso = jwt.getClaimAsString("id_idoso");
        return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("nome"),
                jwt.getClaimAsString("email"), tipo, idIdoso == null ? null : Long.valueOf(idIdoso));
    }
}
