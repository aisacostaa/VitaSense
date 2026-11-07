package br.vitasense;

import br.vitasense.model.Dispositivo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdosoCasaTest {
    private Dispositivo d(String tipo, boolean ligado, boolean conectado) { return new Dispositivo("Aparelho", tipo, "Sala", ligado, conectado, null); }

    @Test void estadoEmLinguagemSimplesConcordaComOGenero() {
        assertEquals("Ligada", d("LUZ", true, true).getEstadoSimples());
        assertEquals("Desligada", d("LUZ", false, true).getEstadoSimples());
        assertEquals("Ligado", d("EXAUSTOR", true, true).getEstadoSimples());
        assertEquals("Desligado", d("AQUECEDOR", false, true).getEstadoSimples());
        assertEquals("Ligada", d("TOMADA", true, true).getEstadoSimples());
    }

    @Test void semConexaoOuComFalhaTemEstadoProprio() {
        assertEquals("Sem conexão", d("LUZ", true, false).getEstadoSimples());
        Dispositivo falha = d("EXAUSTOR", true, true);
        falha.setFalhaDescricao("Falha no motor");
        assertEquals("Com problema", falha.getEstadoSimples());
    }

    @Test void aMesmaRegraDoPainelValeParaOIdoso() {
        Dispositivo falha = d("EXAUSTOR", false, true);
        falha.setFalhaDescricao("Falha no motor");
        assertFalse(falha.permiteAlterar(true));              // desligado + com falha: não pode ligar
        assertFalse(d("LUZ", false, false).permiteAlterar(true));   // sem conexão: nada muda
        assertTrue(d("LUZ", false, true).permiteAlterar(true));
    }
}
