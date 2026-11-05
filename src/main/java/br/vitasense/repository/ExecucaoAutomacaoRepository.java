package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExecucaoAutomacaoRepository extends JpaRepository<ExecucaoAutomacao, Long> {
    List<ExecucaoAutomacao> findTop8ByImovelOrderByMomentoDesc(Imovel imovel);
}
