package br.vitasense;

import br.vitasense.model.Alerta;
import br.vitasense.model.StatusAlerta;
import br.vitasense.service.IdosoService;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class IdosoAvisosTest {
    private Alerta alerta(String titulo, String prioridade, StatusAlerta st) { return new Alerta(titulo, "Descrição técnica.", prioridade, LocalDateTime.now(), st, null); }

    @Test void falhaEDesconexaoViramFrasesSimples() {
        Map<String, String> falha = IdosoService.simplificar(alerta("Falha no dispositivo: Exaustor", "Média", StatusAlerta.NOVO));
        assertEquals("O aparelho Exaustor está com problema.", falha.get("titulo"));
        assertTrue(falha.get("texto").contains("Avise o seu responsável"));

        Map<String, String> off = IdosoService.simplificar(alerta("Dispositivo desconectado: Luz da sala", "Média", StatusAlerta.NOVO));
        assertEquals("Não foi possível conectar a Luz da sala.", off.get("titulo"));
    }

    @Test void alertasAntigosDeDemonstracaoTambemSaoSimplificados() {
        assertEquals("Não foi possível conectar a um aparelho.", IdosoService.simplificar(alerta("Dispositivo da cozinha desconectado", "Média", StatusAlerta.EM_ANDAMENTO)).get("titulo"));
        assertEquals("O sensor detectou água.", IdosoService.simplificar(alerta("Sensor de vazamento detectou água", "Alta", StatusAlerta.EM_ANDAMENTO)).get("titulo"));
    }

    @Test void textoDesconhecidoNaoPerdeInformacao() {
        Map<String, String> r = IdosoService.simplificar(alerta("Outro evento qualquer", "Baixa", StatusAlerta.NOVO));
        assertEquals("Outro evento qualquer", r.get("titulo"));
        assertEquals("Descrição técnica.", r.get("texto"));
    }

    @Test void importanteOuInformativo() {
        assertTrue(IdosoService.importante(alerta("x", "Alta", StatusAlerta.NOVO)));
        assertTrue(IdosoService.importante(alerta("x", "Média", StatusAlerta.EM_ANDAMENTO)));
        assertFalse(IdosoService.importante(alerta("x", "Baixa", StatusAlerta.NOVO)));
        assertFalse(IdosoService.importante(alerta("x", "Média", StatusAlerta.INFORMATIVO)));
    }
}
