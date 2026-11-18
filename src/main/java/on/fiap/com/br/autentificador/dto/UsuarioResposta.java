package on.fiap.com.br.autentificador.dto;

import on.fiap.com.br.autentificador.models.enums.TipoUsuario;
import on.fiap.com.br.autentificador.services.UsuarioAutenticado;

import java.util.List;

/**
 * @param idIdoso preenchido só para FAMILIAR
 */
public record UsuarioResposta(Long id, String nome, String email, TipoUsuario tipo,
                              List<String> permissoes, Long idIdoso) {

    public static UsuarioResposta de(UsuarioAutenticado usuario) {
        return new UsuarioResposta(usuario.id(), usuario.nome(), usuario.email(), usuario.tipo(),
                usuario.tipo().getPermissoes(), usuario.idIdoso());
    }
}
