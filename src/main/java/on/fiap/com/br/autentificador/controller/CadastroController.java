package on.fiap.com.br.autentificador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import on.fiap.com.br.autentificador.configuracoes.ConfiguracaoOpenApi;
import on.fiap.com.br.autentificador.dto.CadastroCuidadorRequisicao;
import on.fiap.com.br.autentificador.dto.CadastroParenteRequisicao;
import on.fiap.com.br.autentificador.dto.ErroResposta;
import on.fiap.com.br.autentificador.dto.TokenResposta;
import on.fiap.com.br.autentificador.dto.UsuarioResposta;
import on.fiap.com.br.autentificador.services.CadastroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao/cadastro")
@Tag(name = "Cadastro", description = "Criação de contas de cuidador e de familiar")
@RequiredArgsConstructor
public class CadastroController {

    private final CadastroService cadastroService;

    @Operation(summary = "Cria a conta de um cuidador",
            description = "Rota pública. A conta já sai logada: a resposta traz os mesmos tokens do login.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/cuidador")
    public ResponseEntity<TokenResposta> cadastrarCuidador(@RequestBody @Valid CadastroCuidadorRequisicao requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroService.cadastrarCuidador(requisicao));
    }

    @Operation(summary = "Cria a conta de um familiar (parente) do idoso",
            description = "Só o cuidador logado, e só para um idoso dele. "
                    + "O familiar depois entra pelo /autenticacao/login com o e-mail e a senha cadastrados.",
            security = @SecurityRequirement(name = ConfiguracaoOpenApi.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token de FAMILIAR, ou idoso de outro cuidador",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Idoso não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou telefone já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/parente")
    public ResponseEntity<UsuarioResposta> cadastrarParente(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestBody @Valid CadastroParenteRequisicao requisicao) {
        Long idCuidador = Long.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroService.cadastrarParente(idCuidador, requisicao));
    }
}
