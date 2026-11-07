package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Regras da área da pessoa acompanhada: resumo da casa em linguagem simples e pedido de ajuda (SOS). */
@Service
public class IdosoService {
    /** Categorias de alerta que o idoso vê como "avisos da casa". */
    public static final List<String> CATEGORIAS_CASA = List.of("Dispositivos", "Sensores", "Conexão");
    /** Categoria do pedido de ajuda: NÃO está na lista de notificações silenciáveis, então nunca é escondida do responsável. */
    public static final String CATEGORIA_SOS = "Emergência";
    /** Sensores só informam; fechadura não é aberta/fechada pela área do idoso (segurança). */
    private static final Set<String> NAO_CONTROLAVEIS = Set.of("SENSOR", "FECHADURA");

    private final DispositivoRepository disp; private final AlertaRepository alertas; private final AlertaService alertaSvc; private final AtividadeRepository ativ;
    public IdosoService(DispositivoRepository d, AlertaRepository a, AlertaService as, AtividadeRepository at) { disp = d; alertas = a; alertaSvc = as; ativ = at; }

    public static String primeiroNome(String nome) { return nome == null || nome.isBlank() ? "" : nome.trim().split("\\s+")[0]; }
    public static String tituloSos(PessoaAcompanhada p) { return "Pedido de ajuda de " + primeiroNome(p.getNome()); }

    /** {tudoOk, mensagem, avisos} */
    public Map<String, Object> resumoCasa(Imovel im) {
        long avisos = alertas.findByImovelOrderByOcorrenciaDesc(im).stream().filter(a -> !a.isResolvido() && CATEGORIAS_CASA.contains(a.getCategoria())).count();
        long aparelhos = disp.findByImovelOrderByIdAsc(im).stream().filter(d -> !d.isConectado() || d.isFalha()).count();
        boolean ok = avisos == 0 && aparelhos == 0;
        String msg = ok ? "Tudo funcionando normalmente"
                : avisos > 0 ? (avisos == 1 ? "Há 1 aviso que precisa da sua atenção" : "Há " + avisos + " avisos que precisam da sua atenção")
                : "Há aparelhos que não estão respondendo";
        return Map.of("tudoOk", ok, "mensagem", msg, "avisos", avisos);
    }

    /** Pedido de ajuda já enviado e ainda não resolvido pelo responsável. */
    public Optional<Alerta> pedidoAberto(PessoaAcompanhada p) {
        String t = tituloSos(p);
        return alertas.findByImovelOrderByOcorrenciaDesc(p.getImovel()).stream().filter(a -> a.getTitulo().equals(t) && !a.isResolvido()).findFirst();
    }

    /** Abre um alerta de emergência de prioridade alta. Se já houver um aberto, não duplica. */
    @Transactional
    public Alerta pedirAjuda(PessoaAcompanhada p) {
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Alerta a = alertaSvc.abrir(p.getImovel(), CATEGORIA_SOS, tituloSos(p), p.getNome() + " pediu ajuda pelo botão \"Preciso de ajuda\".", "Alta",
                p.getNome() + " — acesso por código", "Pedido feito às " + hora + ". Ligue para " + primeiroNome(p.getNome()) + " assim que possível.");
        Atividade at = new Atividade(LocalDateTime.now(), p.getNome() + " pediu ajuda", "Pedido enviado ao responsável.", "LEMBRETE", p.getImovel());
        at.setAutor(p.getNome());
        ativ.save(at);
        return a;
    }

    /** Aparelhos que o idoso pode controlar, agrupados por cômodo (só cômodos que têm algum). */
    public List<Map<String, Object>> comodos(Imovel im) {
        List<Dispositivo> todos = disp.findByImovelOrderByIdAsc(im).stream().filter(d -> !NAO_CONTROLAVEIS.contains(d.getTipo())).toList();
        Set<String> nomes = new LinkedHashSet<>(CasaService.COMODOS);
        todos.forEach(d -> nomes.add(d.getComodo()));
        List<Map<String, Object>> out = new ArrayList<>();
        for (String c : nomes) {
            List<Dispositivo> ds = todos.stream().filter(d -> c.equals(d.getComodo())).toList();
            if (!ds.isEmpty()) out.add(Map.of("nome", c, "total", ds.size(), "dispositivos", ds));
        }
        return out;
    }

    /** Liga/desliga um aparelho da casa da pessoa. Mesma regra do painel: sem conexão ou com falha não liga. */
    @Transactional
    public String alternarDispositivo(PessoaAcompanhada p, Long id) {
        Dispositivo d = disp.findById(id).orElseThrow(() -> new IllegalStateException("Aparelho não encontrado."));
        if (!d.getImovel().getId().equals(p.getImovel().getId()) || NAO_CONTROLAVEIS.contains(d.getTipo()))
            throw new IllegalStateException("Este aparelho não pode ser controlado por aqui.");
        boolean ligar = !d.isLigado();
        if (!d.permiteAlterar(ligar))
            throw new IllegalStateException(!d.isConectado() ? d.getNome() + " não está respondendo. Veja se está ligado na tomada."
                    : d.getNome() + " está com problema e não pode ser ligado. Avise o seu responsável.");
        d.setLigado(ligar); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d);
        Atividade at = new Atividade(LocalDateTime.now(), d.getNome() + (ligar ? " foi ligado" : " foi desligado"),
                "Alterado por " + primeiroNome(p.getNome()) + ".", "DISPOSITIVO", d.getImovel());
        at.setAutor(p.getNome());                                  // o responsável vê QUEM fez
        ativ.save(at);
        return d.getNome() + " " + (d.isFeminino() ? (ligar ? "ligada" : "desligada") : (ligar ? "ligado" : "desligado")) + ".";
    }

    /** "Adicionado por": "Eu", "João (Filho)", "Seu responsável". */
    public static String textoCriador(boolean criadoPelaPessoa, String criadorNome, ContatoEmergencia principal) {
        if (criadoPelaPessoa) return "Eu";
        if (criadorNome == null || criadorNome.isBlank()) return "Seu responsável";
        String parentesco = principal != null && principal.getNome().equalsIgnoreCase(criadorNome) ? principal.getParentesco() : "responsável";
        return primeiroNome(criadorNome) + " (" + parentesco + ")";
    }

    // ================= AVISOS DA CASA =================
    /** Alertas abertos que dizem respeito à casa (aparelhos, sensores, conexão). Resolvidos pelo responsável deixam de aparecer. */
    public List<Alerta> avisosAbertos(Imovel im) {
        return alertas.findByImovelOrderByOcorrenciaDesc(im).stream().filter(a -> !a.isResolvido() && CATEGORIAS_CASA.contains(a.getCategoria())).toList();
    }

    /** "Importante" = pede atenção (prioridade média/alta). O resto é apenas informativo. */
    public static boolean importante(Alerta a) { return a.getStatus() != StatusAlerta.INFORMATIVO && a.isRequerAtencao(); }

    /** Reescreve o alerta técnico do painel em palavras simples: {titulo, texto}. */
    public static Map<String, String> simplificar(Alerta a) {
        String t = a.getTitulo() == null ? "" : a.getTitulo(), baixo = t.toLowerCase();
        if (t.startsWith("Falha no dispositivo: ")) {
            String nome = t.substring("Falha no dispositivo: ".length());
            return Map.of("titulo", "O aparelho " + nome + " está com problema.", "texto", "Ele foi desligado para a sua segurança. Avise o seu responsável.");
        }
        if (t.startsWith("Dispositivo desconectado: ")) {
            String nome = t.substring("Dispositivo desconectado: ".length());
            return Map.of("titulo", "Não foi possível conectar a " + nome + ".", "texto", "O aparelho não respondeu. Confira se está ligado na tomada.");
        }
        if (baixo.contains("desconectado") || baixo.contains("offline"))
            return Map.of("titulo", "Não foi possível conectar a um aparelho.", "texto", "Um aparelho não respondeu. Confira se está ligado na tomada.");
        if (baixo.contains("vazamento") || baixo.contains("água"))
            return Map.of("titulo", "O sensor detectou água.", "texto", "Veja se há água no chão e avise o seu responsável.");
        return Map.of("titulo", t, "texto", a.getDescricao() == null ? "" : a.getDescricao());
    }

    /** A pessoa marca "Já li". O responsável vê isso nos detalhes do alerta e nas atividades. */
    @Transactional
    public void marcarLido(PessoaAcompanhada p, Long alertaId) {
        Alerta a = alertas.findById(alertaId).orElseThrow(() -> new IllegalStateException("Aviso não encontrado."));
        if (!a.getImovel().getId().equals(p.getImovel().getId()) || !CATEGORIAS_CASA.contains(a.getCategoria()))
            throw new IllegalStateException("Este aviso não está disponível para você.");
        if (a.getVistoPelaPessoaEm() != null) return;                 // já lido: não repete o registro
        a.setVistoPelaPessoaEm(LocalDateTime.now());
        alertas.save(a);
        Atividade at = new Atividade(LocalDateTime.now(), primeiroNome(p.getNome()) + " leu um aviso", simplificar(a).get("titulo"), "LEMBRETE", p.getImovel());
        at.setAutor(p.getNome());
        ativ.save(at);
    }
}
