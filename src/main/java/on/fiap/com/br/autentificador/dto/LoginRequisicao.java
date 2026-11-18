package on.fiap.com.br.autentificador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequisicao(

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(max = 72)
        String senha
) {
}
