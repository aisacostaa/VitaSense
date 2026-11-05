package br.vitasense.controller;

import br.vitasense.dto.ConfigForms;
import br.vitasense.model.*;
import br.vitasense.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Configurações: aba "Pessoa acompanhada" (identificação, código de acesso, hábitos, medicamentos, informações, contatos) e aba "Responsável" (conta, notificações, segurança). */
@Controller
@RequestMapping("/configuracoes")
public class ConfiguracoesController extends BaseController {
    private final AcessoIdosoService acesso; private final ConfiguracaoService cfg; private final SessoesService sessoes;
    public ConfiguracoesController(AcessoIdosoService a, ConfiguracaoService c, SessoesService s) { acesso = a; cfg = c; sessoes = s; }

    // ================= tela =================
    @GetMapping
    public String configuracoes(Authentication auth, HttpSession session, Model m,
                                @RequestParam(defaultValue = "pessoa") String aba, @RequestParam(required = false) String editar,
                                @RequestParam(required = false) String med, @RequestParam(required = false) String contato) {
        PessoaAcompanhada p = acesso.garantir(pessoa(auth));
        cfg.garantir(p);
        Usuario u = ctx.usuario(auth);
        Imovel im = p.getImovel();
        List<Medicamento> meds = cfg.medicamentos(p);
        List<ContatoEmergencia> cts = cfg.contatos(p);
        ContatoEmergencia principal = cts.isEmpty() ? null : cts.get(0);

        String resp = im.getAdministrador().getNome();
        if (principal != null && principal.getNome().equalsIgnoreCase(resp)) resp += " · " + principal.getParentesco();
        String cep = im.getCep() == null || im.getCep().length() != 8 ? im.getCep() : im.getCep().substring(0, 5) + "-" + im.getCep().substring(5);

        m.addAttribute("aba", aba.equals("responsavel") ? "responsavel" : "pessoa");
        m.addAttribute("editar", editar);
        m.addAttribute("pessoa", p); m.addAttribute("imovel", im); m.addAttribute("conta", u);
        m.addAttribute("idade", ConfiguracaoService.idade(p.getNascimento(), LocalDate.now()));
        m.addAttribute("responsavelTexto", resp);
        m.addAttribute("endereco", im.getRua() + ", nº " + im.getNumero() + (im.getComplemento() != null ? " (" + im.getComplemento() + ")" : "") + " · " + im.getBairro() + " · " + im.getCidade() + " · CEP " + cep);
        m.addAttribute("iniciaisPessoa", iniciais(p.getNome())); m.addAttribute("iniciaisConta", iniciais(u.getNome()));
        m.addAttribute("urlAcesso", ServletUriComponentsBuilder.fromCurrentContextPath().path("/acesso").toUriString());
        m.addAttribute("medicamentos", meds);
        m.addAttribute("medForm", med != null);
        m.addAttribute("medEdit", med == null || med.equals("novo") ? null : meds.stream().filter(x -> String.valueOf(x.getId()).equals(med)).findFirst().orElse(null));
        m.addAttribute("principal", principal);
        m.addAttribute("outros", cts.size() > 1 ? cts.subList(1, cts.size()) : List.of());
        m.addAttribute("totalContatos", cts.size());
        m.addAttribute("contatoForm", contato != null);
        m.addAttribute("contatoEdit", contato == null || contato.equals("novo") ? null : cts.stream().filter(x -> String.valueOf(x.getId()).equals(contato)).findFirst().orElse(null));
        if (aba.equals("responsavel")) {
            m.addAttribute("prefs", cfg.preferencias(u));
            m.addAttribute("sessoes", sessoes.listar(auth, session.getId()));
        }
        m.addAttribute("ativo", "configuracoes");
        return "configuracoes";
    }

    // ================= pessoa acompanhada =================
    @PostMapping("/pessoa/perfil")
    public String perfil(@Valid @ModelAttribute ConfigForms.Perfil f, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "pessoa", "editar=perfil", "identificacao");
        return exec(ra, destino("pessoa", null, "identificacao"), () -> cfg.salvarPerfil(pessoa(auth), f), "Perfil atualizado.");
    }

    @PostMapping("/pessoa/habitos")
    public String habitos(@Valid @ModelAttribute ConfigForms.Habitos f, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "pessoa", "editar=habitos", "habitos");
        return exec(ra, destino("pessoa", null, "habitos"), () -> cfg.salvarHabitos(pessoa(auth), f), "Hábitos atualizados.");
    }

    @PostMapping("/pessoa/info")
    public String info(@Valid @ModelAttribute ConfigForms.Info f, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "pessoa", "editar=info", "informacoes");
        return exec(ra, destino("pessoa", null, "informacoes"), () -> cfg.salvarInfo(pessoa(auth), f), "Informações atualizadas.");
    }

    @PostMapping("/pessoa/medicamento")
    public String medicamento(@Valid @ModelAttribute ConfigForms.Medicamento f, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "pessoa", "med=" + (f.id() == null ? "novo" : f.id()), "medicamentos");
        return exec(ra, destino("pessoa", null, "medicamentos"), () -> cfg.salvarMedicamento(pessoa(auth), f), f.id() == null ? "Medicamento adicionado." : "Medicamento atualizado.");
    }

    @PostMapping("/pessoa/medicamento/{id}/remover")
    public String removerMedicamento(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        return exec(ra, destino("pessoa", null, "medicamentos"), () -> cfg.removerMedicamento(pessoa(auth), id), "Medicamento removido.");
    }

    @PostMapping("/pessoa/contato")
    public String contato(@Valid @ModelAttribute ConfigForms.Contato f, BindingResult br, Authentication auth, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "pessoa", "contato=" + (f.id() == null ? "novo" : f.id()), "contatos");
        return exec(ra, destino("pessoa", null, "contatos"), () -> cfg.salvarContato(pessoa(auth), f), f.id() == null ? "Contato adicionado." : "Contato atualizado.");
    }

    @PostMapping("/pessoa/contato/{id}/remover")
    public String removerContato(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        return exec(ra, destino("pessoa", null, "contatos"), () -> cfg.removerContato(pessoa(auth), id), "Contato removido.");
    }

    // ================= código de acesso da pessoa acompanhada =================
    @PostMapping("/acesso/regenerar")
    public String regenerar(Authentication auth, RedirectAttributes ra) {
        PessoaAcompanhada p = acesso.regenerar(pessoa(auth));
        ra.addFlashAttribute("ok", "Novo código gerado: " + p.getCodigoFormatado() + ". O código anterior deixou de funcionar.");
        return destino("pessoa", null, "acesso");
    }

    @PostMapping("/acesso/alternar")
    public String alternar(Authentication auth, RedirectAttributes ra) {
        PessoaAcompanhada p = acesso.alternarAtivo(pessoa(auth));
        ra.addFlashAttribute("ok", p.isAcessoAtivo() ? "Acesso ativado. O código volta a funcionar." : "Acesso desativado. O código não funciona até ser reativado.");
        return destino("pessoa", null, "acesso");
    }

    // ================= responsável =================
    @PostMapping("/conta")
    public String conta(@Valid @ModelAttribute ConfigForms.Conta f, BindingResult br, Authentication auth, HttpServletRequest req, RedirectAttributes ra) {
        if (br.hasErrors()) return invalido(ra, br, "responsavel", "editar=conta", "conta");
        try {
            if (cfg.salvarConta(ctx.usuario(auth), f)) {                 // e-mail é o login: entra de novo com o novo e-mail
                SecurityContextHolder.clearContext();
                req.getSession().invalidate();
                return "redirect:/login?logout";
            }
            ra.addFlashAttribute("ok", "Dados da conta atualizados.");
        } catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); return destino("responsavel", "editar=conta", "conta"); }
        req.getSession().removeAttribute(GlobalAdvice.CHAVE);            // nome no topo
        return destino("responsavel", null, "conta");
    }

    @PostMapping("/conta/senha")
    public String senha(@ModelAttribute ConfigForms.Senha f, Authentication auth, HttpSession session, RedirectAttributes ra) {
        return exec(ra, destino("responsavel", null, "seguranca"), () -> {
            cfg.alterarSenha(ctx.usuario(auth), f);
            sessoes.encerrarOutras(auth, session.getId());
        }, "Senha alterada. As outras sessões foram encerradas.");
    }

    @PostMapping("/notificacoes")
    public String notificacoes(@RequestParam Map<String, String> p, Authentication auth, HttpSession session, RedirectAttributes ra) {
        int n = ConfiguracaoService.NOTIFICACOES.size();
        boolean[] ativadas = new boolean[n], painel = new boolean[n];
        for (int i = 0; i < n; i++) { ativadas[i] = "ATIVADAS".equals(p.get("pref" + i)); painel[i] = p.containsKey("painel" + i); }
        cfg.salvarPreferencias(ctx.usuario(auth), ativadas, painel);
        session.removeAttribute(GlobalAdvice.CHAVE);                     // o sino passa a respeitar as preferências
        ra.addFlashAttribute("ok", "Preferências de notificação salvas.");
        return destino("responsavel", null, "notificacoes");
    }

    @PostMapping("/sessoes/encerrar")
    public String encerrarSessoes(Authentication auth, HttpSession session, RedirectAttributes ra) {
        int n = sessoes.encerrarOutras(auth, session.getId());
        ra.addFlashAttribute("ok", n == 0 ? "Não há outras sessões abertas." : n + " sessão(ões) encerrada(s). Esta sessão foi mantida.");
        return destino("responsavel", null, "seguranca");
    }

    // ================= fotos =================
    @GetMapping("/foto/{tipo}")
    public ResponseEntity<byte[]> foto(@PathVariable String tipo, Authentication auth) {
        return cfg.foto(chave(tipo, auth))
                .map(f -> ResponseEntity.ok().contentType(MediaType.parseMediaType(f.getTipoConteudo())).header("Cache-Control", "private, no-cache").body(f.getDados()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/foto/{tipo}")
    public String enviarFoto(@PathVariable String tipo, @RequestParam("arquivo") MultipartFile arquivo, Authentication auth, RedirectAttributes ra) {
        return exec(ra, destino(tipo.equals("usuario") ? "responsavel" : "pessoa", null, tipo.equals("usuario") ? "conta" : "identificacao"), () -> {
            try { cfg.salvarFoto(chave(tipo, auth), arquivo); } catch (IOException e) { throw new IllegalStateException("Não foi possível ler a imagem."); }
        }, "Foto atualizada.");
    }

    // ================= utilitários =================
    /** A foto é sempre da pessoa/usuário do contexto logado (nunca de um id vindo da URL). */
    private String chave(String tipo, Authentication auth) {
        return switch (tipo) {
            case "pessoa" -> "pessoa:" + pessoa(auth).getId();
            case "usuario" -> "usuario:" + ctx.usuario(auth).getId();
            default -> throw new IllegalStateException("Tipo de foto inválido.");
        };
    }
    private static String iniciais(String nome) {
        String[] p = nome.trim().split("\\s+");
        return ("" + p[0].charAt(0) + (p.length > 1 ? p[p.length - 1].charAt(0) : "")).toUpperCase();
    }
    private static String destino(String aba, String extra, String ancora) {
        return "redirect:/configuracoes?aba=" + aba + (extra != null ? "&" + extra : "") + (ancora != null ? "#" + ancora : "");
    }
    private String invalido(RedirectAttributes ra, BindingResult br, String aba, String extra, String ancora) {
        ra.addFlashAttribute("erro", br.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.joining(" ")));
        return destino(aba, extra, ancora);
    }
    /** Executa a ação; regra de negócio violada vira aviso (em vez de página de erro). */
    private String exec(RedirectAttributes ra, String destino, Runnable acao, String ok) {
        try { acao.run(); if (ok != null) ra.addFlashAttribute("ok", ok); }
        catch (IllegalStateException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return destino;
    }
}
