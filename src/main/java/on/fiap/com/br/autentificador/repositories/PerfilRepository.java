package on.fiap.com.br.autentificador.repositories;

import on.fiap.com.br.autentificador.models.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    /** Perfis "Cuidador" e "Familiar" criados pela migration M_AUT_V02. */
    Optional<Perfil> findFirstByNomeOrderByIdAsc(String nome);
}
