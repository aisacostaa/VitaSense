package br.vitasense.repository;

import br.vitasense.model.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    Optional<Imovel> findFirstByAdministradorEmail(String email);
}
