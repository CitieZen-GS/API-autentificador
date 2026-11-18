package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResposta(

        @Schema(description = "JWT para o header Authorization: Bearer nas outras APIs")
        String tokenAcesso,

        @Schema(example = "Bearer")
        String tipoToken,

        @Schema(description = "Validade do tokenAcesso, em segundos", example = "3600")
        long expiraEm,

        @Schema(description = "Usado em /autenticacao/atualizar quando o tokenAcesso expirar")
        String tokenAtualizacao,

        UsuarioResposta usuario
) {
}
