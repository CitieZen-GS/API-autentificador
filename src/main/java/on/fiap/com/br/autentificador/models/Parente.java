package on.fiap.com.br.autentificador.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela criada pela API de funções (M_AJ_V05). O parente faz login como FAMILIAR.
 * O idoso pertence à API de funções, por isso aqui guardamos só o id (sem mapear a entidade).
 */
@Entity
@Table(name = "T_Ajudante_familiar_Parente")
@Getter
@Setter
@NoArgsConstructor
public class Parente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parente")
    private Long id;

    @Column(name = "nm_parente", length = 20)
    private String nome;

    @Column(name = "nr_telefone", length = 18, unique = true)
    private String telefone;

    @Column(name = "ds_email", length = 100, unique = true)
    private String email;

    /** Hash BCrypt da senha (60 caracteres). */
    @Column(name = "ds_senha", length = 100)
    private String senha;

    @Column(name = "id_idoso", nullable = false)
    private Long idIdoso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;
}
