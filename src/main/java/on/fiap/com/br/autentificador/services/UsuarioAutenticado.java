package on.fiap.com.br.autentificador.services;

import on.fiap.com.br.autentificador.models.Cuidador;
import on.fiap.com.br.autentificador.models.Parente;
import on.fiap.com.br.autentificador.models.enums.TipoUsuario;

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
}
