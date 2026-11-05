package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, Long> { List<ContatoEmergencia> findByPessoaOrderByPrioridadeAscIdAsc(PessoaAcompanhada pessoa); }
