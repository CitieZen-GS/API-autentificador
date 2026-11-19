package on.fiap.com.br.autentificador.configuracoes;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação Swagger / OpenAPI.
 * <ul>
 *   <li>Swagger UI: /swagger-ui.html</li>
 *   <li>JSON OpenAPI: /v3/api-docs</li>
 * </ul>
 * As rotas GET /autenticacao/eu e POST /autenticacao/cadastro/parente pedem o JWT no botão "Authorize".
 */
@Configuration
public class ConfiguracaoOpenApi {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI documentacaoApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ajudante Familiar: API de Autenticação")
                        .version("v1")
                        .description("""
                                Login do app e emissão do JWT usado nas outras APIs.

                                **Tipos de usuário**

                                | Cadastro | `usuario.tipo` / claim `roles` | `permissoes` | No app |
                                |---|---|---|---|
                                | Cuidador | `CUIDADOR` | `LEITURA`, `ESCRITA` | telas de cadastro e edição |
                                | Parente | `FAMILIAR` | `LEITURA` | só telas de visualização do idoso `idIdoso` |

                                **Criação de contas**
                                - Cuidador: `POST /autenticacao/cadastro/cuidador` (público). Já devolve os tokens.
                                - Familiar: `POST /autenticacao/cadastro/parente`, feito pelo cuidador logado,
                                  para um idoso dele. Depois o familiar entra com o próprio e-mail e senha.

                                **Fluxo no app**
                                1. `POST /autenticacao/login` com e-mail e senha.
                                2. Guarde `tokenAcesso` e `tokenAtualizacao`.
                                3. Use `usuario.tipo` (ou `usuario.permissoes`) para escolher as telas.
                                4. Envie `Authorization: Bearer <tokenAcesso>` nas chamadas à API de funções.
                                   Ela também aplica as permissões: FAMILIAR recebe 403 ao tentar gravar.
                                5. Ao reabrir o app, `GET /autenticacao/eu` confirma o token e devolve o usuário.
                                6. Recebeu 401? Chame `POST /autenticacao/atualizar`. Se ele também der 401, volte ao login.
                                7. Para sair, `POST /autenticacao/sair` e apague os tokens do aparelho."""))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("tokenAcesso devolvido pelo login")));
    }
}
