package br.vitasense.controller;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.AlertaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/alertas")
public class AlertasController extends BaseController {
    private static final List<String> CATEGORIAS = List.of("Emergência", "Saúde", "Câmeras", "Sensores", "Dispositivos", "Conexão", "Lembretes");
    private final ImovelRepository imoveis; private final UsuarioRepository usuarios; private final AlertaRepository repo; private final AlertaService svc;

    public AlertasController(ImovelRepository i, UsuarioRepository u, AlertaRepository r, AlertaService s) { imoveis = i; usuarios = u; repo = r; svc = s; }

    private String nome(Authentication a) { return usuarios.findByEmail(a.getName()).map(Usuario::getNome).orElse(a.getName()); }
    private String volta(Long sel, String categoria) {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/alertas");
        if (sel != null) b.queryParam("sel", sel);
        if (categoria != null && !categoria.isBlank()) b.queryParam("categoria", categoria);
        return "redirect:" + b.build().encode().toUriString();
    }

    @GetMapping
    public String alertas(Authentication auth, Model m, @RequestParam(required = false) String categoria, @RequestParam(required = false) Long sel) {
        Imovel im = imovel(auth);
        svc.garantir(im);
        LocalDateTime corte = LocalDateTime.now().minusHours(24);
        List<Alerta> todos = repo.findByImovelOrderByOcorrenciaDesc(im);
        List<Alerta> periodo = todos.stream().filter(a -> a.getOcorrencia().isAfter(corte)).toList();
        List<Alerta> lista = periodo.stream().filter(a -> categoria == null || categoria.isBlank() || categoria.equals(a.getCategoria())).toList();

        Alerta det = sel == null ? (lista.isEmpty() ? null : lista.get(0)) : todos.stream().filter(a -> a.getId().equals(sel)).findFirst().orElse(null);
        if (sel != null && det != null) {                         // abrir os detalhes conta como visualizar
            boolean admin = auth.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"));
            if (admin) svc.visualizar(det, nome(auth));
        }

        List<Map<String, Object>> chips = new ArrayList<>();
        chips.add(Map.of("nome", "Todos", "valor", "", "total", periodo.size(), "ativo", categoria == null || categoria.isBlank()));
        for (String c : CATEGORIAS)
            chips.add(Map.of("nome", c, "valor", c, "total", periodo.stream().filter(a -> c.equals(a.getCategoria())).count(), "ativo", c.equals(categoria)));

        m.addAttribute("imovel", im);
        m.addAttribute("periodoTotal", periodo.size());
        m.addAttribute("novos", periodo.stream().filter(Alerta::isNovo).count());
        m.addAttribute("andamento", periodo.stream().filter(a -> a.getStatus() == StatusAlerta.EM_ANDAMENTO).count());
        m.addAttribute("resolvidos", periodo.stream().filter(Alerta::isResolvido).count());
        m.addAttribute("altas", periodo.stream().filter(Alerta::isAlta).count());
        m.addAttribute("naoVisualizados", periodo.stream().filter(a -> !a.isVisualizado()).count());
        m.addAttribute("chips", chips); m.addAttribute("categoria", categoria);
        m.addAttribute("lista", lista); m.addAttribute("det", det);
        m.addAttribute("historico", todos.stream().filter(a -> !a.getOcorrencia().isAfter(corte) && a.isResolvido()).toList());
        m.addAttribute("corte", corte.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        m.addAttribute("ativo", "alertas");
        return "alertas";
    }

    @PostMapping("/visualizar-todos")
    public String visualizarTodos(Authentication auth, @RequestParam(required = false) String categoria, RedirectAttributes ra) {
        int n = svc.visualizarTodos(imovel(auth), nome(auth));
        ra.addFlashAttribute("ok", n + " alerta(s) marcado(s) como visualizado(s). Isso não resolve as ocorrências.");
        return volta(null, categoria);
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) String categoria, Authentication auth, RedirectAttributes ra) {
        try { svc.iniciar(svc.doUsuario(id, auth.getName()), nome(auth)); ra.addFlashAttribute("ok", "Verificação iniciada."); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return volta(id, categoria);
    }

    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam(required = false) String categoria, Authentication auth, RedirectAttributes ra) {
        try { svc.resolver(svc.doUsuario(id, auth.getName()), nome(auth)); ra.addFlashAttribute("ok", "Alerta marcado como resolvido."); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return volta(id, categoria);
    }
}
