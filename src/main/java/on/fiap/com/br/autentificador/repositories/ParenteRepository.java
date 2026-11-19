package on.fiap.com.br.autentificador.repositories;

import on.fiap.com.br.autentificador.models.Parente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParenteRepository extends JpaRepository<Parente, Long> {

    Optional<Parente> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);
}
