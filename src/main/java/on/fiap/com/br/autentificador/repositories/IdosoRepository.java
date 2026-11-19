package on.fiap.com.br.autentificador.repositories;

import on.fiap.com.br.autentificador.models.Idoso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdosoRepository extends JpaRepository<Idoso, Long> {
}
