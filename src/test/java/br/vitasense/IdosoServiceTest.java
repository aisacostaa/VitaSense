package br.vitasense;

import br.vitasense.model.PessoaAcompanhada;
import br.vitasense.service.ConfiguracaoService;
import br.vitasense.service.IdosoService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdosoServiceTest {
    @Test void primeiroNome() {
        assertEquals("Maria", IdosoService.primeiroNome("Maria Helena de Souza"));
        assertEquals("Dona", IdosoService.primeiroNome("  Dona   Maria "));
        assertEquals("", IdosoService.primeiroNome(null));
        assertEquals("", IdosoService.primeiroNome("   "));
    }

    @Test void tituloDoPedidoDeAjudaEFixoPorPessoa() {
        PessoaAcompanhada p = new PessoaAcompanhada(); p.setNome("Maria Helena de Souza");
        assertEquals("Pedido de ajuda de Maria", IdosoService.tituloSos(p));   // mesmo título = o alerta não duplica
    }

    @Test void pedidoDeAjudaNuncaPodeSerSilenciado() {
        // a categoria do SOS não está entre as categorias que o responsável pode silenciar nas notificações
        boolean silenciavel = ConfiguracaoService.NOTIFICACOES.stream().anyMatch(n -> n[0].equals(IdosoService.CATEGORIA_SOS));
        assertFalse(silenciavel);
    }

    @Test void avisosDaCasaSaoSoDeAparelhos() {
        assertTrue(IdosoService.CATEGORIAS_CASA.contains("Dispositivos"));
        assertFalse(IdosoService.CATEGORIAS_CASA.contains("Saúde"));          // saúde não aparece como "aviso da casa"
    }
}
