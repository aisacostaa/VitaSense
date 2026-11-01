package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PessoaAcompanhadaRepository extends JpaRepository<PessoaAcompanhada, Long> {
    Optional<PessoaAcompanhada> findFirstByImovel(Imovel imovel);
}
