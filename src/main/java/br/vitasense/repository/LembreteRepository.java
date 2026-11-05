package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LembreteRepository extends JpaRepository<Lembrete, Long> {
    List<Lembrete> findByPessoa(PessoaAcompanhada pessoa);
    long countByPessoa(PessoaAcompanhada pessoa);
}
