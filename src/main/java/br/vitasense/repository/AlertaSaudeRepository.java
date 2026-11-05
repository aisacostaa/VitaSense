package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertaSaudeRepository extends JpaRepository<AlertaSaude, Long> {
    List<AlertaSaude> findTop10ByPessoaOrderByOcorrenciaDesc(PessoaAcompanhada pessoa);
    long countByPessoa(PessoaAcompanhada pessoa);
}
