package br.vitasense.repository;

import br.vitasense.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AutomacaoRepository extends JpaRepository<Automacao, Long> {
    List<Automacao> findByImovelOrderByIdAsc(Imovel imovel);
    List<Automacao> findByGatilhoAndAtiva(String gatilho, boolean ativa);
    long countByImovel(Imovel imovel);
    /** O dispositivo é usado como equipamento (ação) ou como sensor (gatilho) de alguma rotina? */
    boolean existsByDispositivoOrSensor(Dispositivo dispositivo, Dispositivo sensor);
}
