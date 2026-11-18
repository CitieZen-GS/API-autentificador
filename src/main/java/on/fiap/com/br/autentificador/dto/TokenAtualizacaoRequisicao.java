package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenAtualizacaoRequisicao(

        @Schema(description = "tokenAtualizacao devolvido pelo login ou pela última renovação",
                example = "q3Jt0mH8sY2l1vX9kPz4cA7nE5wR6uB0dF1gT2hJ3kL")
        @NotBlank @Size(max = 200)
        String tokenAtualizacao
) {
}
