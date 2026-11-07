package br.vitasense.service;

import br.vitasense.dto.*;
import br.vitasense.model.*;
import br.vitasense.repository.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LembreteService {

    public static final List<String> CATEGORIAS = List.of(
            "Medicamento",
            "Compromisso",
            "Alimentação",
            "Rotina",
            "Atividade cotidiana",
            "Outro"
    );

    private static final String[] COD = {
            "SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"
    };

    /**
     * Passado este tempo sem confirmação, o responsável é alertado.
     */
    static final int ESPERA_ALERTA_MIN = 30;
    static final int LIMITE_ALERTA_HORAS = 6;

    private final LembreteRepository lembretes;
    private final ConfirmacaoLembreteRepository confirmacoes;
    private final AtividadeRepository ativ;
    private final PessoaAcompanhadaRepository pessoas;
    private final AlertaService alertaSvc;

    public LembreteService(
            LembreteRepository l,
            ConfirmacaoLembreteRepository c,
            AtividadeRepository a,
            PessoaAcompanhadaRepository p,
            AlertaService al
    ) {
        lembretes = l;
        confirmacoes = c;
        ativ = a;
        pessoas = p;
        alertaSvc = al;
    }

    /**
     * Título fixo por lembrete + dia.
     * O alerta não duplica e pode ser encerrado quando a pessoa confirma.
     */
    public static String tituloAlerta(String titulo, LocalDate data) {
        return "Lembrete sem confirmação: " + titulo + " ("
                + data.format(DateTimeFormatter.ofPattern("dd/MM")) + ")";
    }

    public Lembrete doUsuario(Long id, String email) {
        Lembrete l = lembretes.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Lembrete não encontrado.")
                );

        if (!l.getPessoa().getImovel().getAdministrador()
                .getEmail().equals(email)) {
            throw new AccessDeniedException("Sem permissão.");
        }

        return l;
    }

    private Lembrete daPessoa(PessoaAcompanhada p, Long id) {
        return lembretes.findById(id)
                .filter(x -> x.getPessoa().getId().equals(p.getId()))
                .orElseThrow(() ->
                        new IllegalStateException("Lembrete não encontrado.")
                );
    }

    /**
     * Verifica se o lembrete acontece nesta data.
     */
    public boolean ocorreEm(Lembrete l, LocalDate d) {
        if (d.isBefore(l.getData())) {
            return false;
        }

        return switch (l.getRecorrencia()) {
            case "Diariamente" -> true;

            case "Semanalmente" ->
                    (l.getDiasLista().isEmpty()
                            ? List.of(COD[l.getData().getDayOfWeek().getValue() - 1])
                            : l.getDiasLista())
                            .contains(COD[d.getDayOfWeek().getValue() - 1]);

            default -> d.equals(l.getData());
        };
    }

    /**
     * Retorna todas as ocorrências entre duas datas, inclusive,
     * ordenadas por data e horário.
     */
    public List<OcorrenciaLembrete> ocorrencias(
            PessoaAcompanhada p,
            LocalDate de,
            LocalDate ate
    ) {
        List<Lembrete> ls = lembretes.findByPessoa(p);

        Map<String, ConfirmacaoLembrete> conf = new HashMap<>();

        confirmacoes.findByLembretePessoaAndDataBetween(p, de, ate)
                .forEach(c ->
                        conf.put(
                                c.getLembrete().getId() + "|" + c.getData(),
                                c
                        )
                );

        LocalDateTime agora = LocalDateTime.now();

        List<OcorrenciaLembrete> out = new ArrayList<>();

        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1)) {
            for (Lembrete l : ls) {

                if (!ocorreEm(l, d)) {
                    continue;
                }

                ConfirmacaoLembrete c =
                        conf.get(l.getId() + "|" + d);

                if (c != null) {
                    out.add(new OcorrenciaLembrete(
                            l,
                            d,
                            "Confirmado",
                            "Resposta às " + String.format(
                                    "%02dh%02d",
                                    c.getMomento().getHour(),
                                    c.getMomento().getMinute()
                            )
                    ));

                } else if (d.atTime(l.getHorario()).isAfter(agora)) {
                    out.add(new OcorrenciaLembrete(
                            l,
                            d,
                            "Agendado",
                            "Ainda não exibido"
                    ));

                } else {
                    out.add(new OcorrenciaLembrete(
                            l,
                            d,
                            "Não confirmado",
                            "Sem resposta até " + String.format(
                                    "%02dh%02d",
                                    agora.getHour(),
                                    agora.getMinute()
                            )
                    ));
                }
            }
        }

        out.sort(
                Comparator.comparing(OcorrenciaLembrete::getData)
                        .thenComparing(OcorrenciaLembrete::getHorarioTexto)
        );

        return out;
    }

    // =========================================================
    // LADO DO RESPONSÁVEL
    // =========================================================

    @Transactional
    public void salvar(
            PessoaAcompanhada p,
            String email,
            LembreteForm f
    ) {
        Lembrete l = f.id() == null
                ? new Lembrete()
                : doUsuario(f.id(), email);

        if (f.id() == null) {
            l.setCriadoPorNome(p.getImovel().getAdministrador().getNome());
            l.setCriadoPorPessoa(false);
        }

        l.setPessoa(p);
        l.setTitulo(f.titulo().trim());
        l.setCategoria(f.categoria());
        l.setDescricao(f.descricao());
        l.setData(f.data());
        l.setHorario(f.horario());

        l.setRecorrencia(
                f.recorrencia() == null
                        ? "Não se repete"
                        : f.recorrencia()
        );

        l.setDiasSemana(
                l.getRecorrencia().equals("Semanalmente") && f.dias() != null
                        ? String.join(",", f.dias())
                        : null
        );

        l.setMensagem(f.mensagem());
        l.setNotificar(Boolean.TRUE.equals(f.notificar()));

        lembretes.save(l);

        ativ.save(new Atividade(
                LocalDateTime.now(),
                "Lembrete " + (f.id() == null ? "criado" : "atualizado")
                        + ": " + l.getTitulo(),
                "Ação do responsável.",
                "LEMBRETE",
                p.getImovel()
        ));
    }

    @Transactional
    public void excluir(Long id, String email) {
        Lembrete l = doUsuario(id, email);

        confirmacoes.deleteByLembrete(l);
        lembretes.delete(l);

        ativ.save(new Atividade(
                LocalDateTime.now(),
                "Lembrete excluído: " + l.getTitulo(),
                "Ação do responsável.",
                "LEMBRETE",
                l.getPessoa().getImovel()
        ));
    }

    /**
     * Confirmação registrada pelo responsável.
     */
    @Transactional
    public void confirmar(Long id, LocalDate data, String email) {
        Lembrete l = doUsuario(id, email);

        validarConfirmacao(l, data);

        confirmacoes.save(
                new ConfirmacaoLembrete(l, data, LocalDateTime.now())
        );

        alertaSvc.resolverPorTitulo(
                l.getPessoa().getImovel(),
                tituloAlerta(l.getTitulo(), data),
                "confirmação registrada pelo responsável"
        );

        ativ.save(new Atividade(
                LocalDateTime.now(),
                "Lembrete confirmado pelo responsável",
                "Registrado para: " + l.getTitulo() + ".",
                "LEMBRETE",
                l.getPessoa().getImovel()
        ));
    }

    private void validarConfirmacao(Lembrete l, LocalDate data) {
        if (!ocorreEm(l, data)) {
            throw new IllegalStateException(
                    "Este lembrete não acontece nesta data."
            );
        }

        if (data.atTime(l.getHorario()).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Ainda não é a hora deste lembrete."
            );
        }

        if (confirmacoes.existsByLembreteAndData(l, data)) {
            throw new IllegalStateException(
                    "Este lembrete já foi confirmado."
            );
        }
    }

    // =========================================================
    // LADO DA PESSOA ACOMPANHADA
    // =========================================================

    /**
     * A pessoa confirma o lembrete.
     * Reflete no calendário/lista do responsável, nas atividades
     * e encerra o alerta de "sem confirmação".
     */
    @Transactional
    public void confirmarPelaPessoa(
            PessoaAcompanhada p,
            Long id,
            LocalDate data
    ) {
        Lembrete l = daPessoa(p, id);

        validarConfirmacao(l, data);

        confirmacoes.save(
                new ConfirmacaoLembrete(l, data, LocalDateTime.now())
        );

        alertaSvc.resolverPorTitulo(
                p.getImovel(),
                tituloAlerta(l.getTitulo(), data),
                p.getNome() + " confirmou"
        );

        Atividade a = new Atividade(
                LocalDateTime.now(),
                "Lembrete confirmado",
                IdosoService.primeiroNome(p.getNome())
                        + " confirmou: " + l.getTitulo() + ".",
                "LEMBRETE",
                p.getImovel()
        );

        a.setAutor(p.getNome());

        ativ.save(a);
    }

    /**
     * A pessoa cria um lembrete para si.
     * O responsável também o vê, marcado como criado por ela.
     *
     * A data é escolhida pela pessoa no calendário.
     */
    @Transactional
    public void criarPelaPessoa(
            PessoaAcompanhada p,
            String titulo,
            LocalTime horario,
            LocalDate data,
            boolean todoDia
    ) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalStateException(
                    "Escreva o nome do lembrete."
            );
        }

        if (titulo.trim().length() > 80) {
            throw new IllegalStateException(
                    "O nome do lembrete está muito grande (máximo 80 letras)."
            );
        }

        if (horario == null) {
            throw new IllegalStateException(
                    "Escolha o horário do lembrete."
            );
        }

        if (data == null) {
            throw new IllegalStateException(
                    "Escolha a data do lembrete."
            );
        }

        LocalDate hoje = LocalDate.now();

        if (data.isBefore(hoje)) {
            throw new IllegalStateException(
                    "Escolha uma data de hoje em diante."
            );
        }

        if (data.equals(hoje) && !horario.isAfter(LocalTime.now())) {
            throw new IllegalStateException(
                    "Esse horário já passou. Escolha um horário mais tarde ou outra data."
            );
        }

        Lembrete l = new Lembrete();

        l.setPessoa(p);
        l.setTitulo(titulo.trim());
        l.setCategoria("Atividade cotidiana");
        l.setDescricao("Criado pela própria pessoa.");
        l.setData(data);
        l.setHorario(horario);
        l.setRecorrencia(
                todoDia ? "Diariamente" : "Não se repete"
        );
        l.setMensagem(titulo.trim());
        l.setNotificar(true);
        l.setCriadoPorNome(p.getNome());
        l.setCriadoPorPessoa(true);

        lembretes.save(l);

        Atividade a = new Atividade(
                LocalDateTime.now(),
                "Lembrete criado por "
                        + IdosoService.primeiroNome(p.getNome()),
                l.getTitulo() + " às " + l.getHorarioTexto()
                        + (todoDia ? ", todos os dias." : "."),
                "LEMBRETE",
                p.getImovel()
        );

        a.setAutor(p.getNome());

        ativ.save(a);
    }

    /**
     * Compatibilidade com chamadas antigas do método.
     * Mantém o comportamento anterior de Hoje/Amanhã.
     */
    @Transactional
    public void criarPelaPessoa(
            PessoaAcompanhada p,
            String titulo,
            LocalTime horario,
            boolean amanha,
            boolean todoDia
    ) {
        LocalDate data = LocalDate.now();

        if (amanha || (todoDia
                && horario != null
                && !horario.isAfter(LocalTime.now()))) {
            data = data.plusDays(1);
        }

        criarPelaPessoa(p, titulo, horario, data, todoDia);
    }

    // =========================================================
    // ESPELHAMENTO AUTOMÁTICO:
    // LEMBRETE SEM RESPOSTA VIRA ALERTA
    // =========================================================

    /**
     * A cada 5 minutos: lembretes de hoje sem confirmação há mais
     * de 30 minutos (e menos de 6 horas) abrem um alerta para o responsável.
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void verificarPendentes() {
        for (PessoaAcompanhada p : pessoas.findAll()) {
            verificarPendentes(p);
        }
    }

    @Transactional
    public void verificarPendentes(PessoaAcompanhada p) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        for (OcorrenciaLembrete o : ocorrencias(p, hoje, hoje)) {
            LocalDateTime hora = o.getData().atTime(o.getHorario());

            if (o.isNaoConfirmado()
                    && hora.isBefore(agora.minusMinutes(ESPERA_ALERTA_MIN))
                    && hora.isAfter(agora.minusHours(LIMITE_ALERTA_HORAS))) {

                alertaSvc.abrir(
                        p.getImovel(),
                        "Lembretes",
                        tituloAlerta(o.getTitulo(), o.getData()),
                        IdosoService.primeiroNome(p.getNome())
                                + " ainda não confirmou o lembrete das "
                                + o.getHorarioTexto() + ".",
                        "Média",
                        p.getNome() + " — " + o.getTitulo(),
                        "Sem confirmação há mais de "
                                + ESPERA_ALERTA_MIN
                                + " minutos. Confirmar o lembrete encerra este alerta."
                );
            }
        }
    }

    // =========================================================
    // DADOS DE DEMONSTRAÇÃO
    // =========================================================

    /**
     * Cria lembretes de demonstração se a pessoa ainda não tiver nenhum.
     */
    @Transactional
    public void garantir(PessoaAcompanhada p) {
        if (lembretes.countByPessoa(p) > 0) {
            return;
        }

        LocalDate hoje = LocalDate.now();
        LocalDateTime n = LocalDateTime.now();
        String nome = p.getNome().split(" ")[0];

        Lembrete med = criar(
                p,
                "Lembrete de medicamento",
                "Medicamento",
                "Tomar o medicamento da manhã.",
                hoje,
                LocalTime.of(8, 0),
                "Diariamente",
                nome + ", é hora do seu medicamento."
        );

        criar(
                p,
                "Consulta médica",
                "Compromisso",
                "Consulta de rotina.",
                hoje,
                LocalTime.of(10, 0),
                "Não se repete",
                nome + ", você tem consulta hoje."
        );

        criar(
                p,
                "Horário do almoço",
                "Alimentação",
                "Almoço.",
                hoje,
                LocalTime.of(12, 0),
                "Não se repete",
                nome + ", o almoço está servido."
        );

        criar(
                p,
                "Preparar-se para dormir",
                "Rotina",
                "Rotina noturna.",
                hoje,
                LocalTime.of(20, 0),
                "Não se repete",
                nome + ", é hora de se preparar para dormir."
        );

        criar(
                p,
                "Organizar fotografias",
                "Atividade cotidiana",
                "Evento único.",
                hoje.plusDays(1),
                LocalTime.of(15, 0),
                "Não se repete",
                nome + ", que tal organizar as fotografias?"
        );

        criar(
                p,
                "Telefonar para a família",
                "Compromisso",
                "Evento único.",
                hoje.plusDays(3),
                LocalTime.of(16, 0),
                "Não se repete",
                nome + ", hoje é dia de ligar para a família."
        );

        if (hoje.atTime(8, 5).isBefore(n)) {
            confirmacoes.save(
                    new ConfirmacaoLembrete(
                            med,
                            hoje,
                            hoje.atTime(8, 5)
                    )
            );
        }
    }

    private Lembrete criar(
            PessoaAcompanhada p,
            String titulo,
            String cat,
            String desc,
            LocalDate data,
            LocalTime hora,
            String rec,
            String msg
    ) {
        Lembrete l = new Lembrete();

        l.setPessoa(p);
        l.setTitulo(titulo);
        l.setCategoria(cat);
        l.setDescricao(desc);
        l.setData(data);
        l.setHorario(hora);
        l.setRecorrencia(rec);
        l.setMensagem(msg);
        l.setNotificar(true);
        l.setCriadoPorNome(
                p.getImovel().getAdministrador().getNome()
        );
        l.setCriadoPorPessoa(false);

        return lembretes.save(l);
    }
}