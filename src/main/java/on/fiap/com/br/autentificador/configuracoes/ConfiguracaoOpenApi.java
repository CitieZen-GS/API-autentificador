package on.fiap.com.br.autentificador.configuracoes;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação Swagger / OpenAPI.
 * <ul>
 *   <li>Swagger UI: /swagger-ui.html</li>
 *   <li>JSON OpenAPI: /v3/api-docs</li>
 * </ul>
 */
@Configuration
public class ConfiguracaoOpenApi {

    @Bean
    public OpenAPI documentacaoApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ajudante Familiar: API de Autenticação")
                        .version("v1")
                        .description("""
                                Login e emissão do JWT usado nas outras APIs.

                                **Tipos de usuário (claim `roles` do JWT):**
                                - Cuidador: `CUIDADOR`, leitura e escrita
                                - Parente: `FAMILIAR`, somente leitura do idoso vinculado (claim `id_idoso`)

                                Copie o `tokenAcesso` da resposta do login e use no botão **Authorize** da API de funções."""));
    }
}
