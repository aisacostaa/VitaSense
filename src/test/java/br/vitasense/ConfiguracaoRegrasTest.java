package br.vitasense;

import br.vitasense.model.ContatoEmergencia;
import br.vitasense.service.ConfiguracaoService;
import br.vitasense.service.SessoesService;
import br.vitasense.util.Formatos;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoRegrasTest {
    private ContatoEmergencia contato(String nome, boolean ativo) { ContatoEmergencia c = new ContatoEmergencia(); c.setNome(nome); c.setAtivo(ativo); return c; }

    @Test void idadeConsideraOAniversarioDoAno() {
        LocalDate nasc = LocalDate.of(1948, 5, 14);
        assertEquals(78, ConfiguracaoService.idade(nasc, LocalDate.of(2026, 10, 30)));
        assertEquals(77, ConfiguracaoService.idade(nasc, LocalDate.of(2026, 5, 13)));   // ainda não fez aniversário
        assertEquals(0, ConfiguracaoService.idade(null, LocalDate.of(2026, 10, 30)));
    }

    @Test void senhaPrecisaDeOitoCaracteresComLetrasENumeros() {
        assertTrue(ConfiguracaoService.senhaValida("senha1234"));
        assertFalse(ConfiguracaoService.senhaValida("abc12"));          // curta
        assertFalse(ConfiguracaoService.senhaValida("somenteletras"));  // sem número
        assertFalse(ConfiguracaoService.senhaValida("12345678"));       // sem letra
        assertFalse(ConfiguracaoService.senhaValida(null));
    }

    @Test void prioridadesSaoSempreSequenciaisEOPrincipalFicaAtivo() {
        List<ContatoEmergencia> lista = new ArrayList<>(List.of(contato("Ana", false), contato("Beto", true), contato("Carla", true)));
        ConfiguracaoService.renumerar(lista);
        assertEquals(1, lista.get(0).getPrioridade());
        assertEquals(2, lista.get(1).getPrioridade());
        assertEquals(3, lista.get(2).getPrioridade());
        assertTrue(lista.get(0).isAtivo());                              // o contato principal nunca fica inativo
        assertTrue(lista.get(0).isPrincipal());
        assertFalse(lista.get(1).isPrincipal());
        assertEquals("2ª prioridade", lista.get(1).getPrioridadeTexto());
    }

    @Test void reordenarMoveOContatoParaAPosicaoEscolhida() {
        ContatoEmergencia a = contato("Ana", true), b = contato("Beto", true), c = contato("Carla", true);
        List<ContatoEmergencia> lista = new ArrayList<>(List.of(a, b, c));
        lista.remove(c); lista.add(0, c);                                // Carla passa a ser a prioridade 1
        ConfiguracaoService.renumerar(lista);
        assertEquals("Carla", lista.get(0).getNome());
        assertEquals(1, c.getPrioridade()); assertEquals(2, a.getPrioridade()); assertEquals(3, b.getPrioridade());
    }

    @Test void reconheceImagemPeloConteudoENaoPelaExtensao() {
        byte[] jpg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        byte[] texto = "isto nao e uma imagem".getBytes();
        assertEquals("image/jpeg", ConfiguracaoService.tipoImagem(jpg));
        assertEquals("image/png", ConfiguracaoService.tipoImagem(png));
        assertEquals("image/webp", ConfiguracaoService.tipoImagem(webp));
        assertNull(ConfiguracaoService.tipoImagem(texto));
        assertNull(ConfiguracaoService.tipoImagem(null));
    }

    @Test void formataTelefone() {
        assertEquals("(11) 99999-8888", Formatos.telefone("11999998888"));
        assertEquals("(11) 3333-4444", Formatos.telefone("1133334444"));
        assertEquals("Não informado", Formatos.telefone(null));
        assertEquals("Não informado", Formatos.telefone("  "));
    }

    @Test void identificaODispositivoDaSessao() {
        assertEquals("Chrome no computador", SessoesService.dispositivo("Mozilla/5.0 (Windows NT 10.0) Chrome/120 Safari/537"));
        assertEquals("Chrome no celular", SessoesService.dispositivo("Mozilla/5.0 (Linux; Android 13) Mobile Chrome/120"));
        assertEquals("Firefox no computador", SessoesService.dispositivo("Mozilla/5.0 (X11; Linux) Gecko Firefox/121"));
        assertEquals("Navegador no computador", SessoesService.dispositivo(null));
    }
}
