package br.vitasense;

import br.vitasense.model.Automacao;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class AutomacaoRegrasTest {
    private Automacao porHorario(String dias) {
        Automacao a = new Automacao();
        a.setGatilho("HORARIO"); a.setHorario(LocalTime.of(7, 0)); a.setDias(dias); a.setAcao("LIGAR_LUZ");
        return a;
    }

    @Test void semDiasMarcadosValeParaTodosOsDias() {
        Automacao a = porHorario("");
        for (DayOfWeek d : DayOfWeek.values()) assertTrue(a.ocorreEm(d));
        assertEquals("Todos os dias, às 07:00", a.getGatilhoTexto());
    }

    @Test void respeitaOsDiasEscolhidos() {
        Automacao a = porHorario("SEG,QUA");
        assertTrue(a.ocorreEm(DayOfWeek.MONDAY));
        assertTrue(a.ocorreEm(DayOfWeek.WEDNESDAY));
        assertFalse(a.ocorreEm(DayOfWeek.TUESDAY));
        assertEquals("Seg, Qua, às 07:00", a.getGatilhoTexto());
    }

    @Test void textosDeAcaoEAlvo() {
        Automacao a = porHorario("");
        assertEquals("Ligar luz", a.getAcaoTexto());
        a.setAcao("ENVIAR_NOTIFICACAO");
        assertEquals("Enviar notificação", a.getAcaoTexto());
        assertEquals("Notificação no painel", a.getAlvo());                      // sem dispositivo vinculado
    }

    @Test void ausenciaDeMovimentoMostraOsMinutos() {
        Automacao a = new Automacao();
        a.setGatilho("AUSENCIA"); a.setMinutos(30);
        assertEquals("Ausência de movimento por 30 min", a.getGatilhoTexto());
    }

    @Test void nuncaExecutadaTemTextoProprio() { assertEquals("Nunca executada", porHorario("").getUltimaTexto()); }
}
