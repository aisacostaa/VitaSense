package br.vitasense.controller;

import br.vitasense.model.StatusAlerta;
import br.vitasense.model.Usuario;
import br.vitasense.repository.AlertaRepository;
import br.vitasense.service.ConfiguracaoService;
import br.vitasense.service.ContextoUsuario;
import br.vitasense.service.SessoesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Dados que o topo/menu precisa em TODAS as telas. Nome, casa e categorias silenciadas ficam em cache na sessão; só o contador é consultado. */
@ControllerAdvice
public class GlobalAdvice {
    public static final String CHAVE = "vs.resumo";     // quem alterar nome/preferências remove esta chave da sessão
    private final ContextoUsuario ctx; private final AlertaRepository alertas; private final ConfiguracaoService cfg; private final SessoesService sessoes;
    public GlobalAdvice(ContextoUsuario c, AlertaRepository a, ConfiguracaoService cf, SessoesService s) { ctx = c; alertas = a; cfg = cf; sessoes = s; }

    @ModelAttribute
    @SuppressWarnings("unchecked")
    public void comuns(Authentication auth, HttpSession session, HttpServletRequest req, Model m) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) return;
        sessoes.anotar(session.getId(), req.getHeader("User-Agent"));
        HashMap<String, Object> r = (HashMap<String, Object>) session.getAttribute(CHAVE);
        if (r == null || !auth.getName().equals(r.get("email"))) { r = montar(auth); session.setAttribute(CHAVE, r); }
        m.addAttribute("usuarioNome", r.get("nome"));
        m.addAttribute("usuarioPerfil", r.get("perfil"));
        m.addAttribute("iniciais", r.get("iniciais"));
        Long imovelId = (Long) r.get("imovelId");
        if (imovelId != null) {
            List<String> silenciadas = (List<String>) r.get("silenciadas");
            m.addAttribute("alertasPendentes", silenciadas.isEmpty()
                    ? alertas.countByImovelIdAndStatusIn(imovelId, StatusAlerta.AGUARDANDO)
                    : alertas.countByImovelIdAndStatusInAndCategoriaNotIn(imovelId, StatusAlerta.AGUARDANDO, silenciadas));
        }
    }

    private HashMap<String, Object> montar(Authentication auth) {
        Usuario u = ctx.usuario(auth);
        String[] p = u.getNome().trim().split("\\s+");
        HashMap<String, Object> r = new HashMap<>();
        r.put("email", auth.getName()); r.put("nome", u.getNome());
        r.put("perfil", ctx.isAdmin(auth) ? "Responsável" : "Usuário");
        r.put("iniciais", ("" + p[0].charAt(0) + (p.length > 1 ? p[p.length - 1].charAt(0) : "")).toUpperCase());
        r.put("silenciadas", new ArrayList<>(cfg.categoriasSilenciadas(u.getId())));
        try { r.put("imovelId", ctx.imovel(auth).getId()); } catch (IllegalStateException e) { r.put("imovelId", null); }
        return r;
    }
}
