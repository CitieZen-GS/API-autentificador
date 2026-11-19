package on.fiap.com.br.autentificador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SairRequisicao(

        @Schema(description = "tokenAtualizacao atual do aparelho",
                example = "q3Jt0mH8sY2l1vX9kPz4cA7nE5wR6uB0dF1gT2hJ3kL")
        @NotBlank @Size(max = 200)
        String tokenAtualizacao,

        @Schema(description = "Opcional, mas recomendado: o tokenAcesso atual. Se vier, ele para de valer "
                + "na hora em todas as APIs (sem ele, continua valendo até expirar).")
        @Size(max = 4000)
        String tokenAcesso
) {
}
