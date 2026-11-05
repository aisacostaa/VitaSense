package br.vitasense.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Lista e encerra as sessões abertas do responsável (computador, celular...). */
@Service
public class SessoesService {
    private final SessionRegistry registry;
    private final Map<String, String> agentes = new ConcurrentHashMap<>();   // id da sessão -> User-Agent
    public SessoesService(SessionRegistry r) { registry = r; }

    public void anotar(String sessaoId, String userAgent) { agentes.putIfAbsent(sessaoId, userAgent == null ? "" : userAgent); }

    public static String dispositivo(String ua) {
        String u = ua == null ? "" : ua;
        String tipo = u.matches("(?i).*(mobile|android|iphone|ipad).*") ? "no celular" : "no computador";
        String nav = u.contains("Edg/") ? "Edge" : u.contains("Firefox") ? "Firefox" : u.contains("Chrome") ? "Chrome" : u.contains("Safari") ? "Safari" : "Navegador";
        return nav + " " + tipo;
    }

    public List<Map<String, Object>> listar(Authentication auth, String atualId) {
        List<SessionInformation> infos = new ArrayList<>(registry.getAllSessions(auth.getPrincipal(), false));
        infos.sort(Comparator.comparing(SessionInformation::getLastRequest).reversed());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM · HH:mm");
        List<Map<String, Object>> out = new ArrayList<>();
        for (SessionInformation s : infos) {
            boolean atual = s.getSessionId().equals(atualId);
            out.add(Map.of("nome", dispositivo(agentes.get(s.getSessionId())), "atual", atual,
                    "detalhe", (atual ? "Sessão atual" : "Outra sessão") + " · última atividade " + s.getLastRequest().toInstant().atZone(ZoneId.systemDefault()).format(fmt)));
        }
        return out;
    }

    /** Encerra todas as sessões do usuário, menos a atual. Retorna quantas foram encerradas. */
    public int encerrarOutras(Authentication auth, String atualId) {
        int n = 0;
        for (SessionInformation s : registry.getAllSessions(auth.getPrincipal(), false))
            if (!s.getSessionId().equals(atualId)) { s.expireNow(); n++; }
        return n;
    }
}
