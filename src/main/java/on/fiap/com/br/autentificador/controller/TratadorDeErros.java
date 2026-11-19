package on.fiap.com.br.autentificador.controller;

import jakarta.servlet.http.HttpServletRequest;
import on.fiap.com.br.autentificador.dto.ErroResposta;
import on.fiap.com.br.autentificador.services.excecoes.AcessoNegadoException;
import on.fiap.com.br.autentificador.services.excecoes.AutenticacaoException;
import on.fiap.com.br.autentificador.services.excecoes.ConflitoException;
import on.fiap.com.br.autentificador.services.excecoes.RecursoNaoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Converte exceções em respostas JSON padronizadas (mesmo formato da API de funções).
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(AutenticacaoException.class)
    public ResponseEntity<ErroResposta> naoAutorizado(AutenticacaoException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage(), req, List.of());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AcessoNegadoException e, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, e.getMessage(), req, List.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> conflito(ConflitoException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<ErroResposta.CampoInvalido> campos = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResposta.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido: verifique o JSON", req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT,
                "Operação viola uma restrição do banco (registro duplicado ou com dados vinculados)",
                req, List.of());
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, HttpServletRequest req,
                                                  List<ErroResposta.CampoInvalido> campos) {
        ErroResposta corpo = new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
