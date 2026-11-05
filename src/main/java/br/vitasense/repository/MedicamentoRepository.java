package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> { List<Medicamento> findByPessoaOrderByIdAsc(PessoaAcompanhada pessoa); }
