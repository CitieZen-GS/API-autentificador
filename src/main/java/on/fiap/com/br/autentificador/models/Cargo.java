package on.fiap.com.br.autentificador.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import on.fiap.com.br.autentificador.models.conversores.ConversorSimNao;

/**
 * Tabela criada pela API de funções (M_AJ_V01). Aqui é só mapeada.
 * fg_permissao 'S' = pode escrever (cuidador), 'N' = somente leitura (familiar).
 */
@Entity
@Table(name = "T_Ajudante_familiar_Cargo")
@Getter
@Setter
@NoArgsConstructor
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    private Long id;

    @Column(name = "nm_cargo", length = 20)
    private String nome;

    @Convert(converter = ConversorSimNao.class)
    @Column(name = "fg_permissao", length = 1)
    private Boolean permissao;
}
