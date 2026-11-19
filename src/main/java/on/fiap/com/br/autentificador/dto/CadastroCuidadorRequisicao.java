package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar a conta de um cuidador")
public record CadastroCuidadorRequisicao(

        @Schema(example = "Maria")
        @NotBlank @Size(max = 20)
        String nome,

        @Schema(description = "Opcional. Salvo no formato 000.000.000-00", example = "123.456.789-09")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}",
                message = "deve estar no formato 000.000.000-00 ou conter 11 dígitos")
        String cpf,

        @Schema(description = "Salvo em minúsculas", example = "maria@email.com")
        @NotBlank @Email @Size(max = 100)
        String email,

        @Schema(description = "De 6 a 72 caracteres. Salva como hash BCrypt.", example = "senha123")
        @NotBlank @Size(min = 6, max = 72)
        String senha
) {
}
