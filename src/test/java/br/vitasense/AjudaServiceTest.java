package br.vitasense;

import br.vitasense.service.AjudaService;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AjudaServiceTest {
    private final AjudaService svc = new AjudaService(null);          // tutoriais() não usa o banco

    @Test void cincoTutoriaisComIdsUnicosEPassoAPasso() {
        List<Map<String, Object>> ts = svc.tutoriais();
        assertEquals(5, ts.size());
        Set<Object> ids = new HashSet<>();
        for (Map<String, Object> t : ts) {
            assertTrue(ids.add(t.get("id")));                          // sem repetição
            assertFalse(((String) t.get("titulo")).isBlank());
            assertTrue(((List<?>) t.get("passos")).size() >= 3);       // sempre há texto, mesmo sem vídeo
            assertTrue(((String) t.get("video")).startsWith("/video/ajuda/"));
        }
    }

    @Test void soAceitaIdsConhecidos() {
        assertTrue(AjudaService.idValido("luzes"));
        assertTrue(AjudaService.idValido("avisos"));
        assertFalse(AjudaService.idValido("../../etc/passwd"));
        assertFalse(AjudaService.idValido(null));
    }
}
