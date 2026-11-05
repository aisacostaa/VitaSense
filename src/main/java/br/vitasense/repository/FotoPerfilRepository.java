package br.vitasense.repository;

import br.vitasense.model.FotoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FotoPerfilRepository extends JpaRepository<FotoPerfil, Long> { Optional<FotoPerfil> findByChave(String chave); }
