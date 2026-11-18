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
import on.fiap.com.br.autentificador.dto.ErroResposta;
import on.fiap.com.br.autentificador.dto.LoginRequisicao;
import on.fiap.com.br.autentificador.dto.TokenAtualizacaoRequisicao;
import on.fiap.com.br.autentificador.dto.TokenResposta;
import on.fiap.com.br.autentificador.dto.UsuarioResposta;
import on.fiap.com.br.autentificador.services.AutenticacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao")
@Tag(name = "Autenticação", description = "Login do app, renovação e saída")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @Operation(summary = "Faz login e devolve os tokens",
            description = "E-mail de cuidador: tipo CUIDADOR (leitura e escrita). "
                    + "E-mail de parente: tipo FAMILIAR (somente leitura) e idIdoso preenchido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login feito"),
            @ApiResponse(responseCode = "400", description = "E-mail ou senha não informados ou em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/login")
    public TokenResposta login(@RequestBody @Valid LoginRequisicao requisicao) {
        return autenticacaoService.login(requisicao);
    }

    @Operation(summary = "Troca o token de atualização por um novo par de tokens",
            description = "Use quando a API de funções devolver 401 (token de acesso expirado). "
                    + "O token de atualização enviado deixa de valer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novos tokens emitidos"),
            @ApiResponse(responseCode = "400", description = "tokenAtualizacao não informado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "401", description = "Token de atualização inválido, expirado ou já usado: "
                    + "volte para o login",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/atualizar")
    public TokenResposta atualizar(@RequestBody @Valid TokenAtualizacaoRequisicao requisicao) {
        return autenticacaoService.atualizar(requisicao.tokenAtualizacao());
    }

    @Operation(summary = "Sai do app: revoga o token de atualização",
            description = "O token de acesso continua válido até expirar; o app deve apagá-lo do aparelho.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Saída registrada"),
            @ApiResponse(responseCode = "400", description = "tokenAtualizacao não informado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping("/sair")
    public ResponseEntity<Void> sair(@RequestBody @Valid TokenAtualizacaoRequisicao requisicao) {
        autenticacaoService.sair(requisicao.tokenAtualizacao());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Devolve o usuário do token",
            description = "Usado pelo app ao reabrir: confirma que o token guardado ainda vale "
                    + "e diz qual tipo de usuário (telas) liberar.",
            security = @SecurityRequirement(name = ConfiguracaoOpenApi.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content)
    })
    @GetMapping("/eu")
    public UsuarioResposta eu(@AuthenticationPrincipal Jwt jwt) {
        return autenticacaoService.usuarioDoToken(jwt);
    }
}
