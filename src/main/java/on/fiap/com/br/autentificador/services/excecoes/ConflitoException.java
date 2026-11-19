package on.fiap.com.br.autentificador.services.excecoes;

/** Lançada quando o dado já está cadastrado, ex.: e-mail ou CPF repetido (vira HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
