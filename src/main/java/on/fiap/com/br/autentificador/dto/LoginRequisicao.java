package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais de cuidador ou parente")
public record LoginRequisicao(

        @Schema(example = "maria@email.com")
        @NotBlank @Email @Size(max = 100)
        String email,

        @Schema(example = "senha123")
        @NotBlank @Size(max = 72)
        String senha
) {
}
