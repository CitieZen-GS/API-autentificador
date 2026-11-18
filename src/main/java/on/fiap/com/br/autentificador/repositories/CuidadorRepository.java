package on.fiap.com.br.autentificador.repositories;

import on.fiap.com.br.autentificador.models.Cuidador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuidadorRepository extends JpaRepository<Cuidador, Long> {

    Optional<Cuidador> findByEmail(String email);
}
