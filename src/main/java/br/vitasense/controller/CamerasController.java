package br.vitasense.controller;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.CameraService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/cameras")
public class CamerasController extends BaseController {
    private final ImovelRepository imoveis; private final PessoaAcompanhadaRepository pessoas; private final CameraRepository cameras;
    private final EventoCameraRepository eventos; private final CameraService svc;

    public CamerasController(ImovelRepository i, PessoaAcompanhadaRepository p, CameraRepository c, EventoCameraRepository e, CameraService s) {
        imoveis = i; pessoas = p; cameras = c; eventos = e; svc = s;
    }


    @GetMapping
    public String cameras(Authentication auth, Model m, @RequestParam(required = false) Long sel) {
        Imovel im = imovel(auth);
        ControleCamera ctl = svc.garantir(im);
        List<Camera> lista = cameras.findByImovelOrderByIdAsc(im);
        Camera cam = lista.stream().filter(c -> c.getId().equals(sel)).findFirst()
                .or(() -> lista.stream().filter(Camera::isOnline).findFirst()).or(() -> lista.stream().findFirst()).orElse(null);

        boolean sessao = cam != null && cam.getId().equals(ctl.getCameraAudioId());
        String motivo = cam == null ? "nenhuma câmera cadastrada" : !cam.isOnline() ? "a câmera está desconectada"
                : !cam.isAudioBidirecional() ? "esta câmera é somente vídeo" : !ctl.isAcessoAutorizado() ? "o acesso não está autorizado"
                : !ctl.isTransmissaoAtiva() ? "a transmissão está interrompida" : null;

        m.addAttribute("imovel", im);
        m.addAttribute("pessoaNome", pessoas.findFirstByImovel(im).map(PessoaAcompanhada::getNome).orElse("a pessoa acompanhada"));
        m.addAttribute("cameras", lista);
        m.addAttribute("online", lista.stream().filter(Camera::isOnline).count());
        m.addAttribute("cam", cam); m.addAttribute("ctl", ctl);
        m.addAttribute("mic", sessao && ctl.isMicrofoneAtivo()); m.addAttribute("aud", sessao && ctl.isAudioAtivo());
        m.addAttribute("motivo", motivo);
        m.addAttribute("eventos", eventos.findTop5ByCameraImovelOrderByMomentoDesc(im));
        m.addAttribute("agora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm:ss")));
        m.addAttribute("ativo", "cameras");
        return "cameras";
    }

    @PostMapping("/atualizar")
    public String atualizar(Authentication auth, RedirectAttributes ra) {
        ra.addFlashAttribute("ok", svc.atualizar(imovel(auth)));
        return "redirect:/cameras";
    }

    @PostMapping("/{id}/audio/{acao}")
    public String audio(@PathVariable Long id, @PathVariable String acao, Authentication auth, RedirectAttributes ra) {
        try { ra.addFlashAttribute("ok", svc.audio(svc.doUsuario(id, auth.getName()), acao)); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/cameras?sel=" + id;
    }

    @PostMapping("/{id}/reconectar")
    public String reconectar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try { ra.addFlashAttribute("ok", svc.reconectar(svc.doUsuario(id, auth.getName()))); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/cameras?sel=" + id;
    }

    @PostMapping("/permissoes/acesso")
    public String acesso(Authentication auth, RedirectAttributes ra) {
        ra.addFlashAttribute("ok", svc.alternarAcesso(imovel(auth)));
        return "redirect:/cameras";
    }

    @PostMapping("/permissoes/transmissao")
    public String transmissao(Authentication auth, RedirectAttributes ra) {
        ra.addFlashAttribute("ok", svc.alternarTransmissao(imovel(auth)));
        return "redirect:/cameras";
    }
}
