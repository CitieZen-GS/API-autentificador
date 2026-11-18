package on.fiap.com.br.autentificador.services.excecoes;

/** Lançada quando as credenciais ou o token de atualização são inválidos (vira HTTP 401). */
public class AutenticacaoException extends RuntimeException {

    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }
}
