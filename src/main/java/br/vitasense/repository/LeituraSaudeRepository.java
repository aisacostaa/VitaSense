package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface LeituraSaudeRepository extends JpaRepository<LeituraSaude, Long> {
    List<LeituraSaude> findTop7ByPessoaOrderByMomentoDesc(PessoaAcompanhada pessoa);
    List<LeituraSaude> findTop100ByPessoaOrderByMomentoDesc(PessoaAcompanhada pessoa);
    Optional<LeituraSaude> findFirstByPessoaOrderByMomentoDesc(PessoaAcompanhada pessoa);
    long countByPessoa(PessoaAcompanhada pessoa);
}
