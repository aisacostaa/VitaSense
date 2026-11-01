package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findTop3ByImovelOrderByOcorrenciaDesc(Imovel imovel);
    long countByImovelAndStatus(Imovel imovel, StatusAlerta status);
}
