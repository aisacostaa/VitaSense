
package br.vitasense.repository;

import br.vitasense.model.ViMemoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViMemoriaRepository
        extends JpaRepository<ViMemoria, Long> {

    Optional<ViMemoria> findByUsuario_Id(Long usuarioId);
}
