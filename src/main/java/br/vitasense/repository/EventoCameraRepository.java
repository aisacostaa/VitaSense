package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoCameraRepository extends JpaRepository<EventoCamera, Long> {
    List<EventoCamera> findTop5ByCameraImovelOrderByMomentoDesc(Imovel imovel);
    long countByCameraImovel(Imovel imovel);
}
