package br.vitasense.controller;

import br.vitasense.dto.DispositivoForm;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.CasaService;
import jakarta.validation.Valid;
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
@RequestMapping("/casa")
public class CasaController {
    private final CasaService svc; private final DispositivoRepository disp; private final AtividadeRepository ativ;
    public CasaController(CasaService s, DispositivoRepository d, AtividadeRepository a) { svc = s; disp = d; ativ = a; }

    @GetMapping
    public String casa(Authentication auth, Model m,
                       @RequestParam(required = false) String comodo, @RequestParam(required = false) String categoria,
                       @RequestParam(required = false) String conexao, @RequestParam(required = false) String estado,
                       @RequestParam(required = false) Long sel, @RequestParam(required = false) Long editar) {
        Imovel im = svc.imovel(auth.getName());
        List<Dispositivo> todos = disp.findByImovelOrderByIdAsc(im);
        List<Dispositivo> lista = todos.stream()
                .filter(d -> vazio(comodo) || d.getComodo().equals(comodo))
                .filter(d -> vazio(categoria) || d.getCategoria().equals(categoria))
                .filter(d -> vazio(conexao) || conexao.equals("Online") == d.isConectado())
                .filter(d -> vazio(estado) || (estado.equals("Ligado") && d.isLigado()) || (estado.equals("Desligado") && !d.isLigado())
                        || (estado.equals("Com falha") && d.isFalha()))
                .toList();

        // cômodos (fixos + os que existirem nos dispositivos)
        Set<String> nomes = new LinkedHashSet<>(CasaService.COMODOS);
        todos.forEach(d -> nomes.add(d.getComodo()));
        List<Map<String, Object>> cards = new ArrayList<>();
        for (String c : nomes) {
            List<Dispositivo> ds = todos.stream().filter(d -> c.equals(d.getComodo())).toList();
            cards.add(Map.of("nome", c, "total", ds.size(), "ligados", ds.stream().filter(Dispositivo::isLigado).count(),
                    "conectados", ds.stream().filter(Dispositivo::isConectado).count()));
        }
        long online = todos.stream().filter(Dispositivo::isConectado).count();

        Dispositivo det = sel != null ? todos.stream().filter(d -> d.getId().equals(sel)).findFirst().orElse(null)
                : todos.stream().filter(Dispositivo::isFalha).findFirst().or(() -> todos.stream().findFirst()).orElse(null);

        m.addAttribute("imovel", im);
        m.addAttribute("agora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")));
        m.addAttribute("cards", cards); m.addAttribute("nomesComodos", nomes);
        m.addAttribute("categorias", CasaService.CATEGORIAS.keySet());
        m.addAttribute("total", todos.size()); m.addAttribute("online", online);
        m.addAttribute("offline", todos.size() - online); m.addAttribute("falhas", todos.stream().filter(Dispositivo::isFalha).count());
        m.addAttribute("ligados", todos.stream().filter(Dispositivo::isLigado).count());
        m.addAttribute("luzesLigadas", todos.stream().filter(d -> d.getTipo().equals("LUZ") && d.isLigado()).count());
        m.addAttribute("lista", lista);
        m.addAttribute("filtrosAtivos", Arrays.stream(new String[]{comodo, categoria, conexao, estado}).filter(s -> !vazio(s)).count());
        m.addAttribute("fComodo", comodo); m.addAttribute("fCategoria", categoria); m.addAttribute("fConexao", conexao); m.addAttribute("fEstado", estado);
        m.addAttribute("det", det);
        m.addAttribute("historico", det == null ? List.of() : ativ.findTop3ByImovelAndTituloStartingWithOrderByMomentoDesc(im, det.getNome()));
        m.addAttribute("edit", editar == null ? null : todos.stream().filter(d -> d.getId().equals(editar)).findFirst().orElse(null));
        m.addAttribute("ativo", "casa");
        return "casa";
    }

    @PostMapping("/dispositivos/salvar")
    public String salvar(@Valid @ModelAttribute DispositivoForm form, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (!CasaService.CATEGORIAS.containsKey(form.categoria())) br.rejectValue("categoria", "cat", "Categoria inválida.");
        if (br.hasErrors()) {
            ra.addFlashAttribute("erros", br.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList());
            return "redirect:/casa#cadastro";
        }
        svc.salvar(auth.getName(), form);
        ra.addFlashAttribute("ok", form.id() == null ? "Dispositivo cadastrado com sucesso." : "Dispositivo atualizado.");
        return "redirect:/casa#equipamentos";
    }

    @PostMapping("/dispositivos/{id}/remover")
    public String remover(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        svc.remover(id, auth.getName());
        ra.addFlashAttribute("ok", "Dispositivo removido.");
        return "redirect:/casa#equipamentos";
    }

    @PostMapping("/remoto/{cmd}")
    public String remoto(@PathVariable String cmd, @RequestParam(required = false) List<Long> ids, Authentication auth, RedirectAttributes ra) {
        String e = auth.getName();
        int n = switch (cmd) {
            case "apagar-luzes" -> svc.apagarLuzes(e);
            case "luzes-corredor" -> svc.acenderCorredor(e);
            case "rotina-noturna" -> svc.rotinaNoturna(e);
            case "desativar" -> svc.desativar(ids, e);
            default -> throw new NoSuchElementException("Comando desconhecido.");
        };
        ra.addFlashAttribute("ok", n + " dispositivo(s) alterado(s) pelo controle remoto.");
        return "redirect:/casa#remoto";
    }

    private boolean vazio(String s) { return s == null || s.isBlank(); }
}
