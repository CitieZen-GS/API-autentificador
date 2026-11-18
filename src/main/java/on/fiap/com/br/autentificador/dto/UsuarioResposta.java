package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import on.fiap.com.br.autentificador.models.enums.TipoUsuario;
import on.fiap.com.br.autentificador.services.UsuarioAutenticado;

import java.util.List;

@Schema(description = "Usuário logado. O app decide as telas por tipo ou permissoes.")
public record UsuarioResposta(

        @Schema(description = "id_cuidador ou id_parente, conforme o tipo", example = "1")
        Long id,

        @Schema(example = "Maria")
        String nome,

        @Schema(example = "maria@email.com")
        String email,

        @Schema(description = "CUIDADOR: leitura e escrita. FAMILIAR: somente leitura.")
        TipoUsuario tipo,

        @Schema(description = "CUIDADOR: [LEITURA, ESCRITA]. FAMILIAR: [LEITURA].",
                example = "[\"LEITURA\", \"ESCRITA\"]")
        List<String> permissoes,

        @Schema(description = "Só para FAMILIAR: o idoso que o parente acompanha. Nulo para CUIDADOR.",
                nullable = true)
        Long idIdoso
) {

    public static UsuarioResposta de(UsuarioAutenticado usuario) {
        return new UsuarioResposta(usuario.id(), usuario.nome(), usuario.email(), usuario.tipo(),
                usuario.tipo().getPermissoes(), usuario.idIdoso());
    }
}
