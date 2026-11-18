package on.fiap.com.br.autentificador.dto;

/**
 * @param tokenAcesso      JWT enviado no header Authorization: Bearer
 * @param expiraEm         validade do token de acesso, em segundos
 * @param tokenAtualizacao usado em /autenticacao/atualizar para pegar um novo token de acesso
 */
public record TokenResposta(String tokenAcesso, String tipoToken, long expiraEm, String tokenAtualizacao,
                            UsuarioResposta usuario) {
}
