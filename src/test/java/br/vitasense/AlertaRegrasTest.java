package br.vitasense;

import br.vitasense.model.Alerta;
import br.vitasense.model.StatusAlerta;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AlertaRegrasTest {
    private Alerta alerta(String prioridade, StatusAlerta st) { return new Alerta("t", "d", prioridade, LocalDateTime.now(), st, null); }

    @Test void nivelDePrioridade() {
        assertEquals("Alta", alerta("Alta", StatusAlerta.NOVO).getNivel());
        assertEquals("Média", alerta("Média", StatusAlerta.NOVO).getNivel());
        assertEquals("Baixa", alerta("Baixa", StatusAlerta.NOVO).getNivel());
    }

    @Test void aceitaTextosAntigosDeAlertasLegados() {
        assertEquals("Média", alerta("Prioridade média", StatusAlerta.PENDENTE).getNivel());
        assertEquals("Baixa", alerta("Informativa", StatusAlerta.INFORMATIVO).getNivel());
        assertEquals("Prioridade média", alerta("Prioridade média", StatusAlerta.PENDENTE).getPrioridade());
    }

    @Test void requerAtencaoSoQuandoNaoForBaixa() {
        assertTrue(alerta("Alta", StatusAlerta.NOVO).isRequerAtencao());
        assertTrue(alerta("Média", StatusAlerta.NOVO).isRequerAtencao());
        assertFalse(alerta("Baixa", StatusAlerta.NOVO).isRequerAtencao());
    }

    @Test void estadosEViewsDoAlerta() {
        assertTrue(alerta("Alta", StatusAlerta.NOVO).isNovo());
        assertTrue(alerta("Alta", StatusAlerta.PENDENTE).isNovo());          // legado conta como "novo"
        assertFalse(alerta("Alta", StatusAlerta.EM_ANDAMENTO).isNovo());
        assertTrue(alerta("Alta", StatusAlerta.RESOLVIDO).isResolvido());
        assertFalse(alerta("Alta", StatusAlerta.NOVO).isVisualizado());       // visualizar é independente do estado
    }

    @Test void contadorDoMenuConsideraNovosEPendentes() {
        assertTrue(StatusAlerta.AGUARDANDO.contains(StatusAlerta.NOVO));
        assertTrue(StatusAlerta.AGUARDANDO.contains(StatusAlerta.PENDENTE));
        assertFalse(StatusAlerta.AGUARDANDO.contains(StatusAlerta.EM_ANDAMENTO));
        assertFalse(StatusAlerta.AGUARDANDO.contains(StatusAlerta.RESOLVIDO));
    }
}
