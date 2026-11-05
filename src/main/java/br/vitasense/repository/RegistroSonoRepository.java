package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RegistroSonoRepository extends JpaRepository<RegistroSono, Long> {
    Optional<RegistroSono> findFirstByPessoaOrderByRegistradoEmDesc(PessoaAcompanhada pessoa);
    List<RegistroSono> findTop30ByPessoaOrderByRegistradoEmDesc(PessoaAcompanhada pessoa);
}
