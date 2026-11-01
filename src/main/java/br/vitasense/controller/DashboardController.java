package br.vitasense.controller;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    private final ImovelRepository imoveis; private final PessoaAcompanhadaRepository pessoas; private final LeituraSaudeRepository leituras;
    private final CameraRepository cameras; private final DispositivoRepository dispositivos; private final AlertaRepository alertas;
    private final AtividadeRepository atividades;

    public DashboardController(ImovelRepository i, PessoaAcompanhadaRepository p, LeituraSaudeRepository l, CameraRepository c,
                               DispositivoRepository d, AlertaRepository a, AtividadeRepository at) {
        imoveis = i; pessoas = p; leituras = l; cameras = c; dispositivos = d; alertas = a; atividades = at;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        Imovel im = imoveis.findFirstByAdministradorEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Este usuário não tem imóvel cadastrado."));
        PessoaAcompanhada pessoa = pessoas.findFirstByImovel(im).orElse(null);

        String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR")));
        m.addAttribute("hoje", Character.toUpperCase(hoje.charAt(0)) + hoje.substring(1));
        m.addAttribute("agora", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        m.addAttribute("imovel", im);
        m.addAttribute("pessoa", pessoa);

        // saúde
        List<LeituraSaude> ls = pessoa == null ? List.of() : leituras.findTop7ByPessoaOrderByMomentoDesc(pessoa);
        m.addAttribute("leitura", ls.isEmpty() ? null : ls.get(0));
        List<LeituraSaude> asc = new ArrayList<>(ls); Collections.reverse(asc);
        m.addAttribute("serieBpm", serie(asc, LeituraSaude::getBpm));
        m.addAttribute("serieSis", serie(asc, LeituraSaude::getSistolica));
        m.addAttribute("serieOxi", serie(asc, LeituraSaude::getOxigenacao));

        // câmeras
        List<Camera> cs = cameras.findByImovelOrderByIdAsc(im);
        m.addAttribute("camera", cs.isEmpty() ? null : cs.get(0));
        m.addAttribute("camerasTotal", cs.size());
        m.addAttribute("camerasOnline", cs.stream().filter(Camera::isOnline).count());

        // dispositivos
        List<Dispositivo> ds = dispositivos.findByImovelOrderByIdAsc(im);
        m.addAttribute("dispositivosTotal", ds.size());
        m.addAttribute("dispositivosConectados", ds.stream().filter(Dispositivo::isConectado).count());
        m.addAttribute("dispositivos", ds.stream().limit(4).toList());

        // alertas e atividades
        List<Alerta> as = alertas.findTop3ByImovelOrderByOcorrenciaDesc(im);
        m.addAttribute("alertas", as);
        m.addAttribute("alertasInfo", as.stream().filter(a -> a.getStatus() == StatusAlerta.INFORMATIVO).count());
        m.addAttribute("pendentes", alertas.countByImovelAndStatus(im, StatusAlerta.PENDENTE));
        m.addAttribute("atividades", atividades.findTop4ByImovelOrderByMomentoDesc(im));
        m.addAttribute("ativo", "dashboard");
        return "dashboard";
    }

    /** Liga/desliga um dispositivo (só o administrador dono do imóvel). */
    @PostMapping("/dispositivos/{id}/alternar")
    public String alternar(@PathVariable Long id, Authentication auth, @RequestHeader(value = "Referer", required = false) String ref) {
        Dispositivo d = dispositivos.findById(id).orElseThrow();
        if (!d.getImovel().getAdministrador().getEmail().equals(auth.getName()))
            throw new AccessDeniedException("Sem permissão para este dispositivo.");
        if (d.isConectado()) {
            d.setLigado(!d.isLigado());
            dispositivos.save(d);
            atividades.save(new Atividade(LocalDateTime.now(), d.getNome() + (d.isLigado() ? " foi ligado" : " foi desligado"),
                    "Alterado manualmente pelo responsável.", "DISPOSITIVO", d.getImovel()));
        }
        return ref != null && ref.contains("/casa") ? "redirect:/casa#equipamentos" : "redirect:/dashboard";
    }

    private String serie(List<LeituraSaude> l, ToIntFunction<LeituraSaude> f) {
        return l.stream().map(x -> String.valueOf(f.applyAsInt(x))).collect(Collectors.joining(","));
    }
}
