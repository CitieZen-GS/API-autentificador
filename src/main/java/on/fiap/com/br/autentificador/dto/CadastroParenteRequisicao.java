package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para o cuidador criar a conta de um familiar do idoso")
public record CadastroParenteRequisicao(

        @Schema(example = "Ana")
        @NotBlank @Size(max = 20)
        String nome,

        @Schema(description = "Opcional", example = "(11) 98765-4321")
        @Size(max = 18)
        @Pattern(regexp = "[0-9()+\\-\\s]*", message = "deve conter só números, espaços, parênteses, + e -")
        String telefone,

        @Schema(description = "Salvo em minúsculas", example = "ana@email.com")
        @NotBlank @Email @Size(max = 100)
        String email,

        @Schema(description = "De 6 a 72 caracteres. Salva como hash BCrypt.", example = "senha123")
        @NotBlank @Size(min = 6, max = 72)
        String senha,

        @Schema(description = "Idoso que o familiar vai acompanhar. Precisa ser do cuidador logado.", example = "1")
        @NotNull @Positive
        Long idIdoso
) {
}
