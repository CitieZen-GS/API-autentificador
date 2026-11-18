package on.fiap.com.br.autentificador.models.enums;

import java.util.List;

/**
 * Tipo de usuário que faz login. O nome vira a claim "roles" do JWT,
 * que a API de funções converte em ROLE_CUIDADOR / ROLE_FAMILIAR.
 */
public enum TipoUsuario {

    /** Registrado em T_Ajudante_familiar_Cuidador: leitura e escrita. */
    CUIDADOR(List.of("LEITURA", "ESCRITA")),

    /** Registrado em T_Ajudante_familiar_Parente: somente leitura, só do idoso vinculado. */
    FAMILIAR(List.of("LEITURA"));

    private final List<String> permissoes;

    TipoUsuario(List<String> permissoes) {
        this.permissoes = permissoes;
    }

    public List<String> getPermissoes() {
        return permissoes;
    }
}
