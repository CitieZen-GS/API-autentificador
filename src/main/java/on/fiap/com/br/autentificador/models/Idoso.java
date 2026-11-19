package on.fiap.com.br.autentificador.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Tabela da API de funções (M_AJ_V04), mapeada só para leitura: a autenticação nunca grava idosos.
 * Usada no cadastro de parente para conferir se o idoso existe e é do cuidador logado.
 */
@Entity
@Immutable
@Table(name = "T_Ajudante_familiar_Idoso")
@Getter
@NoArgsConstructor
public class Idoso {

    @Id
    @Column(name = "id_idoso")
    private Long id;

    @Column(name = "nm_idoso")
    private String nome;

    @Column(name = "id_cuidador")
    private Long idCuidador;
}
