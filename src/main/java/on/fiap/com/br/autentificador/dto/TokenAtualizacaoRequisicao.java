package on.fiap.com.br.autentificador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenAtualizacaoRequisicao(

        @NotBlank @Size(max = 200)
        String tokenAtualizacao
) {
}
