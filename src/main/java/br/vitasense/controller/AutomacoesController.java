package br.vitasense.controller;

import br.vitasense.dto.AutomacaoForm;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.AutomacaoService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/automacoes")
public class AutomacoesController extends BaseController {
    private final ImovelRepository imoveis; private final AutomacaoRepository repo; private final ExecucaoAutomacaoRepository historico;
    private final DispositivoRepository disp; private final AutomacaoService svc;

    public AutomacoesController(ImovelRepository i, AutomacaoRepository r, ExecucaoAutomacaoRepository h, DispositivoRepository d, AutomacaoService s) {
        imoveis = i; repo = r; historico = h; disp = d; svc = s;
    }

    @GetMapping
    public String automacoes(Authentication auth, Model m, @RequestParam(required = false) Long editar) {
        Imovel im = imovel(auth);
        svc.garantir(im);
        List<Automacao> lista = repo.findByImovelOrderByIdAsc(im);
        List<Dispositivo> todos = disp.findByImovelOrderByIdAsc(im);
        long ativas = lista.stream().filter(Automacao::isAtiva).count();
        m.addAttribute("imovel", im);
        m.addAttribute("agora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")));
        m.addAttribute("lista", lista); m.addAttribute("ativas", ativas); m.addAttribute("inativas", lista.size() - ativas);
        m.addAttribute("dispositivos", todos.stream().filter(d -> !d.getTipo().equals("SENSOR")).toList());
        m.addAttribute("sensores", todos.stream().filter(d -> d.getTipo().equals("SENSOR")).toList());
        m.addAttribute("historico", historico.findTop8ByImovelOrderByMomentoDesc(im));
        m.addAttribute("edit", editar == null ? null : lista.stream().filter(a -> a.getId().equals(editar)).findFirst().orElse(null));
        m.addAttribute("dias", Automacao.DIAS);
        m.addAttribute("ativo", "automacoes");
        return "automacoes";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute AutomacaoForm form, BindingResult br, Authentication auth, RedirectAttributes ra) {
        Imovel im = imovel(auth);
        List<String> erros = new ArrayList<>(br.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList());
        if (erros.isEmpty()) erros.addAll(svc.validar(im, form));
        if (!erros.isEmpty()) { ra.addFlashAttribute("erros", erros); return "redirect:/automacoes#criar"; }
        svc.salvar(im, auth.getName(), form);
        ra.addFlashAttribute("ok", form.id() == null ? "Automação criada e ativada." : "Automação atualizada.");
        return "redirect:/automacoes";
    }

    @PostMapping("/{id}/alternar")
    public String alternar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ra.addFlashAttribute("ok", svc.alternar(svc.doUsuario(id, auth.getName())) ? "Rotina ativada." : "Rotina desativada.");
        return "redirect:/automacoes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        svc.excluir(svc.doUsuario(id, auth.getName()));
        ra.addFlashAttribute("ok", "Rotina excluída.");
        return "redirect:/automacoes";
    }

    @PostMapping("/{id}/executar")
    public String executar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ExecucaoAutomacao ex = svc.executar(svc.doUsuario(id, auth.getName()), "manual");
        ra.addFlashAttribute(ex.isSucesso() ? "ok" : "erro", ex.getRotinaNome() + ": " + ex.getTitulo() + ". " + ex.getDetalhe());
        return "redirect:/automacoes#historico";
    }
}
