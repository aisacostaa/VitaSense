package br.vitasense.repository;

import br.vitasense.model.PessoaAcompanhada;
import br.vitasense.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    /** Acesso (perfil COMUM) vinculado a uma pessoa acompanhada. */
    Optional<Usuario> findFirstByPessoa(PessoaAcompanhada pessoa);
}
