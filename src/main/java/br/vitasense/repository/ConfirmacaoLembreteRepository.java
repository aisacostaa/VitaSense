package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ConfirmacaoLembreteRepository extends JpaRepository<ConfirmacaoLembrete, Long> {
    List<ConfirmacaoLembrete> findByLembretePessoaAndDataBetween(PessoaAcompanhada pessoa, LocalDate de, LocalDate ate);
    boolean existsByLembreteAndData(Lembrete lembrete, LocalDate data);
    void deleteByLembrete(Lembrete lembrete);
}
