package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeituraSaudeRepository extends JpaRepository<LeituraSaude, Long> {
    List<LeituraSaude> findTop7ByPessoaOrderByMomentoDesc(PessoaAcompanhada pessoa);
}
