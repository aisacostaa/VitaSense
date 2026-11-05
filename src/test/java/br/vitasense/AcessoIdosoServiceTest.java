package br.vitasense;

import br.vitasense.config.TentativasAcesso;
import br.vitasense.model.PessoaAcompanhada;
import br.vitasense.service.AcessoIdosoService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AcessoIdosoServiceTest {
    @Test void normalizaQualquerFormaDeDigitar() {
        assertEquals("12345678", AcessoIdosoService.normalizar("1234-5678"));
        assertEquals("12345678", AcessoIdosoService.normalizar(" 1234 5678 "));
        assertEquals("12345678", AcessoIdosoService.normalizar("12345678"));
        assertEquals("", AcessoIdosoService.normalizar(null));
        assertEquals("", AcessoIdosoService.normalizar("abc"));
    }

    @Test void formataCodigoComHifen() {
        assertEquals("1234-5678", AcessoIdosoService.formatar("12345678"));
        assertEquals("—", AcessoIdosoService.formatar(null));
        assertEquals("—", AcessoIdosoService.formatar("123"));
    }

    @Test void pessoaSemCodigoNaoTemFormato() {
        PessoaAcompanhada p = new PessoaAcompanhada();
        assertEquals("—", p.getCodigoFormatado());
        assertTrue(p.isAcessoAtivo());                       // linhas antigas (null) contam como ativas
        p.setCodigoAcesso("00120034");
        assertEquals("0012-0034", p.getCodigoFormatado());   // zeros à esquerda são preservados
        p.setAcessoAtivo(false);
        assertFalse(p.isAcessoAtivo());
    }

    @Test void cincoErrosBloqueiamESucessoLibera() {
        TentativasAcesso t = new TentativasAcesso();
        for (int i = 0; i < 4; i++) t.falha("1.2.3.4");
        assertFalse(t.bloqueado("1.2.3.4"));
        t.falha("1.2.3.4");
        assertTrue(t.bloqueado("1.2.3.4"));
        assertFalse(t.bloqueado("9.9.9.9"));                 // outra origem não é afetada
        t.sucesso("1.2.3.4");
        assertFalse(t.bloqueado("1.2.3.4"));
    }
}
