package on.fiap.com.br.autentificador.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Refresh token emitido no login. Pertence a um cuidador OU a um parente (nunca aos dois).
 * Guardamos só o hash SHA-256 do token: se o banco vazar, os tokens não podem ser usados.
 * Logout = preencher dataRevogacao.
 */
@Entity
@Table(name = "T_Ajudante_familiar_Token_Atualizacao")
@Getter
@Setter
@NoArgsConstructor
public class TokenAtualizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long id;

    /** SHA-256 em hexadecimal (64 caracteres). */
    @Column(name = "ds_token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuidador")
    private Cuidador cuidador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parente")
    private Parente parente;

    /** Preenchida pelo DEFAULT do banco. */
    @Column(name = "dt_criacao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "dt_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "dt_revogacao")
    private LocalDateTime dataRevogacao;
}
