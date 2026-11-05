package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SmartwatchRepository extends JpaRepository<Smartwatch, Long> {
    Optional<Smartwatch> findFirstByPessoa(PessoaAcompanhada pessoa);
}
