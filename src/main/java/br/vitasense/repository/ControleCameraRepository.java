package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ControleCameraRepository extends JpaRepository<ControleCamera, Long> {
    Optional<ControleCamera> findFirstByImovel(Imovel imovel);
}
