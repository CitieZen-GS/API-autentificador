package on.fiap.com.br.autentificador.services.excecoes;

/** Lançada quando o usuário logado não pode mexer naquele registro (vira HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
