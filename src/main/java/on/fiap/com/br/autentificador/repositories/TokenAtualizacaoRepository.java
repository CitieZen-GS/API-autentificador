package on.fiap.com.br.autentificador.repositories;

import on.fiap.com.br.autentificador.models.TokenAtualizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenAtualizacaoRepository extends JpaRepository<TokenAtualizacao, Long> {

    Optional<TokenAtualizacao> findByTokenHash(String tokenHash);
}
