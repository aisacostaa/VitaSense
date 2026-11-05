package br.vitasense.controller;

import br.vitasense.dto.*;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.LembreteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lembretes")
public class LembretesController extends BaseController {
    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private final ImovelRepository imoveis; private final PessoaAcompanhadaRepository pessoas; private final LembreteRepository repo; private final LembreteService svc;

    public LembretesController(ImovelRepository i, PessoaAcompanhadaRepository p, LembreteRepository r, LembreteService s) { imoveis = i; pessoas = p; repo = r; svc = s; }

    private static String cap(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    private static String n(long q) { return q == 1 ? "1 lembrete" : q + " lembretes"; }

    @GetMapping
    public String lembretes(Authentication auth, Model m,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
                            @RequestParam(required = false) String mes, @RequestParam(required = false) Long editar) {
        PessoaAcompanhada p = pessoa(auth);
        svc.garantir(p);
        LocalDateTime agora = LocalDateTime.now();
        LocalDate sel = dia != null ? dia : agora.toLocalDate();
        YearMonth ym;
        try { ym = mes != null ? YearMonth.parse(mes) : YearMonth.from(sel); } catch (Exception e) { ym = YearMonth.from(sel); }

        // calendário: 6 semanas começando no domingo
        LocalDate ini = ym.atDay(1); ini = ini.minusDays(ini.getDayOfWeek().getValue() % 7);
        LocalDate fim = ini.plusDays(41);
        Map<LocalDate, Long> contagem = svc.ocorrencias(p, ini, fim).stream().collect(Collectors.groupingBy(OcorrenciaLembrete::getData, Collectors.counting()));
        List<Map<String, Object>> celulas = new ArrayList<>();
        for (LocalDate d = ini; !d.isAfter(fim); d = d.plusDays(1)) {
            boolean fora = !YearMonth.from(d).equals(ym); long q = contagem.getOrDefault(d, 0L);
            celulas.add(Map.of("iso", d.toString(), "rotulo", fora ? d.format(DateTimeFormatter.ofPattern("d MMM", PT)).replace(".", "") : String.valueOf(d.getDayOfMonth()),
                    "fora", fora, "sel", d.equals(sel), "hoje", d.equals(agora.toLocalDate()), "texto", q == 0 ? "" : "• " + n(q)));
        }

        List<OcorrenciaLembrete> doDia = svc.ocorrencias(p, sel, sel);
        List<OcorrenciaLembrete> proximos = svc.ocorrencias(p, agora.toLocalDate(), agora.toLocalDate().plusDays(3)).stream()
                .filter(o -> o.getData().atTime(o.getHorario()).isAfter(agora)).toList();

        // próximos dias com lembretes (agenda abaixo do calendário)
        Map<LocalDate, List<OcorrenciaLembrete>> agenda = svc.ocorrencias(p, sel.plusDays(1), sel.plusDays(14)).stream()
                .collect(Collectors.groupingBy(OcorrenciaLembrete::getData, TreeMap::new, Collectors.toList()));
        List<Map<String, Object>> proxDias = new ArrayList<>();
        for (var e : agenda.entrySet()) {
            if (proxDias.size() == 2) break;
            proxDias.add(Map.of("titulo", e.getKey().format(DateTimeFormatter.ofPattern("dd/MM")) + " · " + n(e.getValue().size()) + " agendado" + (e.getValue().size() > 1 ? "s" : ""),
                    "itens", e.getValue().stream().map(o -> o.getHorarioTexto() + " — " + o.getTitulo() + " · " + (o.getRecorrencia().equals("Não se repete") ? "Evento único" : o.getRecorrencia())).toList()));
        }

        m.addAttribute("pessoa", p);
        m.addAttribute("agora", agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH'h'mm")));
        m.addAttribute("refTexto", agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm")));
        m.addAttribute("selData", sel.format(DateTimeFormatter.ofPattern("dd/MM")));
        m.addAttribute("selTexto", sel.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT)));
        m.addAttribute("selIso", sel.toString()); m.addAttribute("diaAnt", sel.minusDays(1).toString()); m.addAttribute("diaProx", sel.plusDays(1).toString());
        m.addAttribute("mesTitulo", cap(ym.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT))));
        m.addAttribute("mesAtual", ym.toString()); m.addAttribute("mesAnt", ym.minusMonths(1).toString()); m.addAttribute("mesProx", ym.plusMonths(1).toString());
        m.addAttribute("celulas", celulas);
        m.addAttribute("total", doDia.size());
        m.addAttribute("confirmados", doDia.stream().filter(OcorrenciaLembrete::isConfirmado).count());
        m.addAttribute("pendentes", doDia.stream().filter(OcorrenciaLembrete::isNaoConfirmado).count());
        m.addAttribute("agendados", doDia.stream().filter(OcorrenciaLembrete::isAgendado).count());
        m.addAttribute("proximos", proximos.size());
        m.addAttribute("proxFim", agora.toLocalDate().plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM")));
        m.addAttribute("doDia", doDia); m.addAttribute("proxDias", proxDias);
        m.addAttribute("categorias", LembreteService.CATEGORIAS);
        m.addAttribute("edit", editar == null ? null : repo.findById(editar).filter(l -> l.getPessoa().getId().equals(p.getId())).orElse(null));
        m.addAttribute("formData", sel.toString());
        m.addAttribute("ativo", "lembretes");
        return "lembretes";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute LembreteForm form, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (form.categoria() != null && !LembreteService.CATEGORIAS.contains(form.categoria())) br.rejectValue("categoria", "cat", "Categoria inválida.");
        if (br.hasErrors()) {
            ra.addFlashAttribute("erros", br.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList());
            return "redirect:/lembretes#cadastro";
        }
        svc.salvar(pessoa(auth), auth.getName(), form);
        ra.addFlashAttribute("ok", form.id() == null ? "Lembrete criado." : "Lembrete atualizado.");
        return "redirect:/lembretes?dia=" + form.data();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, @RequestParam(required = false) String dia, Authentication auth, RedirectAttributes ra) {
        svc.excluir(id, auth.getName());
        ra.addFlashAttribute("ok", "Lembrete excluído.");
        return "redirect:/lembretes" + (dia != null ? "?dia=" + dia : "");
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia, Authentication auth, RedirectAttributes ra) {
        try { svc.confirmar(id, dia, auth.getName()); ra.addFlashAttribute("ok", "Confirmação registrada."); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/lembretes?dia=" + dia;
    }
}
