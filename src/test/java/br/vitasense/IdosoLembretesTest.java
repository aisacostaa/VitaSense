package br.vitasense;

import br.vitasense.model.ContatoEmergencia;
import br.vitasense.service.IdosoService;
import br.vitasense.service.LembreteService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class IdosoLembretesTest {
    private ContatoEmergencia principal(String nome, String parentesco) { ContatoEmergencia c = new ContatoEmergencia(); c.setNome(nome); c.setParentesco(parentesco); return c; }

    @Test void adicionadoPor() {
        ContatoEmergencia joao = principal("João Pedro", "Filho");
        assertEquals("Eu", IdosoService.textoCriador(true, "Maria", joao));
        assertEquals("João (Filho)", IdosoService.textoCriador(false, "João Pedro", joao));        // responsável é o contato principal
        assertEquals("Carlos (responsável)", IdosoService.textoCriador(false, "Carlos Alberto", joao));
        assertEquals("Seu responsável", IdosoService.textoCriador(false, null, joao));              // lembretes antigos
    }

    @Test void tituloDoAlertaDeLembreteSemConfirmacaoEFixoPorDia() {
        String hoje = LembreteService.tituloAlerta("Tomar remédio", LocalDate.of(2026, 10, 30));
        assertEquals("Lembrete sem confirmação: Tomar remédio (30/10)", hoje);
        assertNotEquals(hoje, LembreteService.tituloAlerta("Tomar remédio", LocalDate.of(2026, 10, 31)));   // outro dia = outro alerta
    }
}
