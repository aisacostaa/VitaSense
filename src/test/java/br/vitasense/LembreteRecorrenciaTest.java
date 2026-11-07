package br.vitasense;

import br.vitasense.model.Lembrete;
import br.vitasense.service.LembreteService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class LembreteRecorrenciaTest {
    private final LembreteService svc = new LembreteService(null, null, null, null, null);   // ocorreEm() não usa repositórios
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);            // segunda-feira

    private Lembrete lembrete(String recorrencia, String dias) {
        Lembrete l = new Lembrete();
        l.setData(SEGUNDA); l.setHorario(LocalTime.of(8, 0)); l.setRecorrencia(recorrencia); l.setDiasSemana(dias);
        return l;
    }

    @Test void naoSeRepeteAconteceSoNaData() {
        Lembrete l = lembrete("Não se repete", null);
        assertTrue(svc.ocorreEm(l, SEGUNDA));
        assertFalse(svc.ocorreEm(l, SEGUNDA.plusDays(1)));
        assertFalse(svc.ocorreEm(l, SEGUNDA.minusDays(1)));
    }

    @Test void diarioAconteceTodoDiaAPartirDaData() {
        Lembrete l = lembrete("Diariamente", null);
        assertTrue(svc.ocorreEm(l, SEGUNDA));
        assertTrue(svc.ocorreEm(l, SEGUNDA.plusDays(10)));
        assertFalse(svc.ocorreEm(l, SEGUNDA.minusDays(1)));                       // nunca antes do início
    }

    @Test void semanalRespeitaOsDiasMarcados() {
        Lembrete l = lembrete("Semanalmente", "SEG,QUA");
        assertTrue(svc.ocorreEm(l, SEGUNDA));
        assertFalse(svc.ocorreEm(l, SEGUNDA.plusDays(1)));                        // terça
        assertTrue(svc.ocorreEm(l, SEGUNDA.plusDays(2)));                         // quarta
        assertTrue(svc.ocorreEm(l, SEGUNDA.plusDays(7)));                         // segunda seguinte
        assertFalse(svc.ocorreEm(l, SEGUNDA.minusDays(5)));                       // quarta anterior ao início
    }

    @Test void semanalSemDiasUsaODiaDaSemanaDaData() {
        Lembrete l = lembrete("Semanalmente", "");
        assertTrue(svc.ocorreEm(l, SEGUNDA.plusDays(14)));
        assertFalse(svc.ocorreEm(l, SEGUNDA.plusDays(3)));
    }

    @Test void horarioEmTexto() { assertEquals("08h00", lembrete("Não se repete", null).getHorarioTexto()); }
}
