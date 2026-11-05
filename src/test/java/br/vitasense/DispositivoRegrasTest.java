package br.vitasense;

import br.vitasense.model.Dispositivo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DispositivoRegrasTest {
    private Dispositivo luz(boolean conectado) { return new Dispositivo("Luz da sala", "LUZ", "Sala", false, conectado, null); }

    @Test void dispositivoConectadoPodeSerLigado() {
        assertTrue(luz(true).permiteAlterar(true));
        assertNull(luz(true).motivoBloqueio());
    }

    @Test void semConexaoNadaMuda() {
        Dispositivo d = luz(false);
        assertFalse(d.permiteAlterar(true));
        assertFalse(d.permiteAlterar(false));
        assertEquals("dispositivo sem conexão", d.motivoBloqueio());
        assertEquals("Desconhecido", d.getEstado());
    }

    @Test void comFalhaSoPodeDesligar() {
        Dispositivo d = new Dispositivo("Exaustor", "EXAUSTOR", "Banheiro", true, true, null);
        d.setFalhaDescricao("Falha no motor");
        assertTrue(d.isFalha());
        assertFalse(d.permiteAlterar(true));
        assertTrue(d.permiteAlterar(false));
        assertEquals("Falha no motor", d.getEstado());
        assertTrue(d.motivoBloqueio().contains("Falha no motor"));
    }

    @Test void categoriasPorTipo() {
        assertEquals("Iluminação", luz(true).getCategoria());
        assertEquals("Aquecimento", new Dispositivo("A", "AQUECEDOR", "Banheiro", false, true, null).getCategoria());
        assertEquals("Climatização", new Dispositivo("A", "AR", "Quarto", false, true, null).getCategoria());
        assertEquals("Ventilação", new Dispositivo("E", "EXAUSTOR", "Banheiro", false, true, null).getCategoria());
    }
}
