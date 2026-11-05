package br.vitasense.controller;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.SaudeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/saude")
public class SaudeController extends BaseController {
    private final ImovelRepository imoveis; private final PessoaAcompanhadaRepository pessoas; private final SaudeService svc;
    private final SmartwatchRepository relogios; private final LeituraSaudeRepository leituras;
    private final RegistroSonoRepository sonos; private final AlertaSaudeRepository alertas;

    public SaudeController(ImovelRepository i, PessoaAcompanhadaRepository p, SaudeService s, SmartwatchRepository r,
                           LeituraSaudeRepository l, RegistroSonoRepository so, AlertaSaudeRepository a) {
        imoveis = i; pessoas = p; svc = s; relogios = r; leituras = l; sonos = so; alertas = a;
    }

    private PessoaAcompanhada pessoa(Imovel im) {
        return pessoas.findFirstByImovel(im).orElseThrow(() -> new IllegalStateException("Nenhuma pessoa acompanhada cadastrada."));
    }

    @GetMapping
    public String saude(Authentication auth, Model m) {
        PessoaAcompanhada p = pessoa(imovel(auth));
        svc.garantir(p);
        m.addAttribute("pessoa", p);
        m.addAttribute("relogio", relogios.findFirstByPessoa(p).orElse(null));
        m.addAttribute("leitura", leituras.findFirstByPessoaOrderByMomentoDesc(p).orElse(null));
        m.addAttribute("total", leituras.countByPessoa(p));
        m.addAttribute("sono", sonos.findFirstByPessoaOrderByRegistradoEmDesc(p).orElse(null));
        m.addAttribute("alertas", alertas.findTop10ByPessoaOrderByOcorrenciaDesc(p));
        m.addAttribute("ativo", "saude");
        return "saude";
    }

    @GetMapping("/historico")
    public String historico(Authentication auth, Model m, @RequestParam(defaultValue = "todas") String metrica) {
        PessoaAcompanhada p = pessoa(imovel(auth));
        svc.garantir(p);
        m.addAttribute("pessoa", p);
        m.addAttribute("metrica", metrica);
        m.addAttribute("lista", leituras.findTop100ByPessoaOrderByMomentoDesc(p));
        m.addAttribute("sonos", sonos.findTop30ByPessoaOrderByRegistradoEmDesc(p));
        m.addAttribute("ativo", "saude");
        return "saude-historico";
    }

    @PostMapping("/atualizar")
    public String atualizar(Authentication auth, RedirectAttributes ra) {
        Imovel im = imovel(auth);
        svc.atualizar(pessoa(im), im);
        ra.addFlashAttribute("ok", "Dados atualizados: nova leitura recebida do smartwatch.");
        return "redirect:/saude";
    }

    @PostMapping("/verificar")
    public String verificar(Authentication auth, RedirectAttributes ra) {
        ra.addFlashAttribute("ok", svc.verificar(pessoa(imovel(auth))));
        return "redirect:/saude";
    }
}
