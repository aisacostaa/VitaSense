package br.vitasense.controller;

import br.vitasense.model.*;
import br.vitasense.service.*;
import br.vitasense.dto.OcorrenciaLembrete;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Área da pessoa acompanhada (entra por código).
 * Telas: início, minha casa, lembretes, avisos, ajuda e Vi.
 */
@Controller
@RequestMapping("/idoso")
public class IdosoController extends BaseController {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");

    /**
     * Seções ainda não construídas.
     */
    private static final Map<String, String> EM_BREVE =
            Map.of("vi", "Vi, sua assistente");

    private final IdosoService svc;
    private final LembreteService lembretes;
    private final ConfiguracaoService cfg;
    private final AjudaService ajuda;

    public IdosoController(
            IdosoService s,
            LembreteService l,
            ConfiguracaoService c,
            AjudaService aj
    ) {
        svc = s;
        lembretes = l;
        cfg = c;
        ajuda = aj;
    }

    /**
     * Dados que toda tela do idoso mostra: data por extenso e hora.
     */
    private void base(Model m, String ativa) {
        String data = LocalDate.now().format(
                DateTimeFormatter.ofPattern(
                        "EEEE, d 'de' MMMM 'de' yyyy", PT
                )
        );

        m.addAttribute(
                "hoje",
                Character.toUpperCase(data.charAt(0)) + data.substring(1)
        );

        m.addAttribute(
                "hora",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        m.addAttribute("ativa", ativa);

        // Data mínima permitida no calendário de novos lembretes.
        m.addAttribute("hojeIso", LocalDate.now().toString());
    }

    @GetMapping
    public String inicio(Authentication auth, Model m) {
        PessoaAcompanhada p = pessoa(auth);

        lembretes.garantir(p);
        cfg.garantir(p);

        LocalDate hoje = LocalDate.now();

        base(m, "inicio");

        m.addAttribute("nome", IdosoService.primeiroNome(p.getNome()));
        m.addAttribute("casa", svc.resumoCasa(p.getImovel()));
        m.addAttribute("lembretesHoje", lembretes.ocorrencias(p, hoje, hoje));

        m.addAttribute(
                "contato",
                cfg.contatos(p).stream()
                        .filter(ContatoEmergencia::isAtivo)
                        .findFirst()
                        .orElse(null)
        );

        m.addAttribute("pedido", svc.pedidoAberto(p).orElse(null));

        return "idoso";
    }

    /**
     * Botão "Preciso de ajuda": abre um alerta de emergência para o responsável.
     */
    @PostMapping("/ajuda/sos")
    public String sos(Authentication auth, RedirectAttributes ra) {
        svc.pedirAjuda(pessoa(auth));

        ra.addFlashAttribute(
                "ok",
                "Pedido de ajuda enviado. Fique tranquilo(a): seu responsável foi avisado."
        );

        return "redirect:/idoso";
    }

    @GetMapping("/casa")
    public String casa(Authentication auth, Model m) {
        PessoaAcompanhada p = pessoa(auth);

        base(m, "casa");

        m.addAttribute("comodos", svc.comodos(p.getImovel()));

        return "idoso-casa";
    }

    @PostMapping("/casa/dispositivos/{id}/alternar")
    public String alternar(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra
    ) {
        try {
            ra.addFlashAttribute(
                    "ok",
                    svc.alternarDispositivo(pessoa(auth), id)
            );
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/idoso/casa";
    }

    /**
     * Lista os lembretes da data selecionada.
     * Se nenhuma data for informada, mostra os lembretes de hoje.
     */
    @GetMapping("/lembretes")
    public String lembretes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dia,
            Authentication auth,
            Model m
    ) {
        PessoaAcompanhada p = pessoa(auth);

        lembretes.garantir(p);
        cfg.garantir(p);

        LocalDate hoje = LocalDate.now();
        LocalDate selecionada = dia == null ? hoje : dia;

        List<OcorrenciaLembrete> lista =
                lembretes.ocorrencias(p, selecionada, selecionada);

        ContatoEmergencia principal = cfg.contatos(p)
                .stream()
                .findFirst()
                .orElse(null);

        Map<Long, String> criador = new HashMap<>();

        lista.forEach(o ->
                criador.put(
                        o.getId(),
                        IdosoService.textoCriador(
                                o.isCriadoPelaPessoa(),
                                o.getCriadoPorNome(),
                                principal
                        )
                )
        );

        long concluidos = lista.stream()
                .filter(OcorrenciaLembrete::isConfirmado)
                .count();

        base(m, "lembretes");

        m.addAttribute("lista", lista);
        m.addAttribute("criador", criador);
        m.addAttribute("concluidos", concluidos);
        m.addAttribute("pendentes", lista.size() - concluidos);

        m.addAttribute("ehHoje", selecionada.equals(hoje));
        m.addAttribute(
                "dataSelecionada",
                selecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        m.addAttribute("dataIso", selecionada.toString());

        return "idoso-lembretes";
    }

    /**
     * A pessoa confirma o lembrete.
     * A confirmação aparece para o responsável e encerra o alerta de
     * "sem confirmação".
     */
    @PostMapping("/lembretes/{id}/confirmar")
    public String confirmarLembrete(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dia,
            Authentication auth,
            RedirectAttributes ra
    ) {
        LocalDate data = dia == null ? LocalDate.now() : dia;

        try {
            lembretes.confirmarPelaPessoa(pessoa(auth), id, data);
            ra.addFlashAttribute("ok", "Lembrete confirmado. Muito bem!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/idoso/lembretes?dia=" + data;
    }

    /**
     * A pessoa cria um lembrete para si.
     * O responsável também o vê, marcado como criado por ela.
     */
    @PostMapping("/lembretes/novo")
    public String novoLembrete(
            @RequestParam(required = false) String titulo,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horario,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data,

            @RequestParam(defaultValue = "uma") String repete,

            Authentication auth,
            RedirectAttributes ra
    ) {
        LocalDate dataEscolhida = data == null ? LocalDate.now() : data;

        try {
            lembretes.criarPelaPessoa(
                    pessoa(auth),
                    titulo,
                    horario,
                    dataEscolhida,
                    "todo-dia".equals(repete)
            );

            ra.addFlashAttribute(
                    "ok",
                    "Lembrete criado. O seu responsável também vai ver."
            );

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/idoso/lembretes?dia=" + dataEscolhida;
    }

    @GetMapping("/avisos")
    public String avisos(Authentication auth, Model m) {
        PessoaAcompanhada p = pessoa(auth);

        List<Map<String, Object>> importantes = new java.util.ArrayList<>();
        List<Map<String, Object>> informativos = new java.util.ArrayList<>();

        for (Alerta a : svc.avisosAbertos(p.getImovel())) {
            Map<String, String> txt = IdosoService.simplificar(a);

            Map<String, Object> item = Map.of(
                    "id", a.getId(),
                    "titulo", txt.get("titulo"),
                    "texto", txt.get("texto"),
                    "hora", a.getOcorrenciaTexto(),
                    "lido", a.getVistoPelaPessoaEm() != null,
                    "aparelho", "Dispositivos".equals(a.getCategoria())
            );

            (IdosoService.importante(a) ? importantes : informativos)
                    .add(item);
        }

        base(m, "avisos");

        m.addAttribute("importantes", importantes);
        m.addAttribute("informativos", informativos);

        return "idoso-avisos";
    }

    /**
     * "Já li": o responsável passa a ver que a pessoa leu o aviso.
     */
    @PostMapping("/avisos/{id}/lido")
    public String avisoLido(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes ra
    ) {
        try {
            svc.marcarLido(pessoa(auth), id);
            ra.addFlashAttribute(
                    "ok",
                    "Certo! O seu responsável vai saber que você leu."
            );
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/idoso/avisos";
    }

    @GetMapping("/ajuda")
    public String ajuda(Authentication auth, Model m) {
        PessoaAcompanhada p = pessoa(auth);

        cfg.garantir(p);

        base(m, "ajuda");

        m.addAttribute("tutoriais", ajuda.tutoriais());

        m.addAttribute(
                "contato",
                cfg.contatos(p).stream()
                        .filter(ContatoEmergencia::isAtivo)
                        .findFirst()
                        .orElse(null)
        );

        return "idoso-ajuda";
    }

    /**
     * Chamado pela página quando a pessoa abre um tutorial.
     */
    @PostMapping("/ajuda/visto/{id}")
    @ResponseBody
    public ResponseEntity<Void> tutorialVisto(
            @PathVariable String id,
            Authentication auth
    ) {
        if (!AjudaService.idValido(id)) {
            return ResponseEntity.badRequest().build();
        }

        ajuda.registrarVisto(pessoa(auth), id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{secao}")
    public String emBreve(
            @PathVariable String secao,
            Model m
    ) {
        String titulo = EM_BREVE.get(secao);

        if (titulo == null) {
            throw new NoSuchElementException("Página não encontrada.");
        }

        base(m, secao);
        m.addAttribute("titulo", titulo);

        return "idoso-em-breve";
    }
}