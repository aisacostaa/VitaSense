package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findTop3ByImovelOrderByOcorrenciaDesc(Imovel imovel);
    List<Alerta> findByImovelOrderByOcorrenciaDesc(Imovel imovel);
    long countByImovelAndStatus(Imovel imovel, StatusAlerta status);
    long countByImovelAndStatusIn(Imovel imovel, Collection<StatusAlerta> status);
    long countByImovelIdAndStatusIn(Long imovelId, Collection<StatusAlerta> status);
    /** Contador do sino já descontando as categorias que o responsável silenciou. */
    long countByImovelIdAndStatusInAndCategoriaNotIn(Long imovelId, Collection<StatusAlerta> status, Collection<String> categorias);
}
