package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    List<Atividade> findTop4ByImovelOrderByMomentoDesc(Imovel imovel);
    List<Atividade> findTop3ByImovelAndTituloStartingWithOrderByMomentoDesc(Imovel imovel, String prefixo);
}
