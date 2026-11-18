package on.fiap.com.br.autentificador.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import on.fiap.com.br.autentificador.dto.LoginRequisicao;
import on.fiap.com.br.autentificador.dto.TokenAtualizacaoRequisicao;
import on.fiap.com.br.autentificador.dto.TokenResposta;
import on.fiap.com.br.autentificador.services.AutenticacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao")
@Tag(name = "Autenticação", description = "Login e emissão de tokens JWT")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @Operation(summary = "Faz login e devolve o token de acesso",
            description = "Cuidador recebe a role CUIDADOR (leitura e escrita). "
                    + "Parente recebe a role FAMILIAR (somente leitura) e a claim id_idoso.")
    @PostMapping("/login")
    public TokenResposta login(@RequestBody @Valid LoginRequisicao requisicao) {
        return autenticacaoService.login(requisicao);
    }

    @Operation(summary = "Troca o token de atualização por um novo par de tokens")
    @PostMapping("/atualizar")
    public TokenResposta atualizar(@RequestBody @Valid TokenAtualizacaoRequisicao requisicao) {
        return autenticacaoService.atualizar(requisicao.tokenAtualizacao());
    }

    @Operation(summary = "Sai: revoga o token de atualização")
    @PostMapping("/sair")
    public ResponseEntity<Void> sair(@RequestBody @Valid TokenAtualizacaoRequisicao requisicao) {
        autenticacaoService.sair(requisicao.tokenAtualizacao());
        return ResponseEntity.noContent().build();
    }
}
