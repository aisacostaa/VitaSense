package br.vitasense.repository;

import br.vitasense.model.PreferenciaNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Long> { List<PreferenciaNotificacao> findByUsuarioId(Long usuarioId); }
