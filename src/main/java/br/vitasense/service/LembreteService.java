package br.vitasense.service;

import br.vitasense.dto.*;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Regras dos lembretes: recorrência, estado de cada ocorrência (Agendado/Confirmado/Não confirmado) e confirmação. */
@Service
public class LembreteService {
    public static final List<String> CATEGORIAS = List.of("Medicamento", "Compromisso", "Alimentação", "Rotina", "Atividade cotidiana", "Outro");
    private static final String[] COD = {"SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"}; // índice = DayOfWeek.getValue()-1

    private final LembreteRepository lembretes; private final ConfirmacaoLembreteRepository confirmacoes; private final AtividadeRepository ativ;
    public LembreteService(LembreteRepository l, ConfirmacaoLembreteRepository c, AtividadeRepository a) { lembretes = l; confirmacoes = c; ativ = a; }

    public Lembrete doUsuario(Long id, String email) {
        Lembrete l = lembretes.findById(id).orElseThrow(() -> new IllegalStateException("Lembrete não encontrado."));
        if (!l.getPessoa().getImovel().getAdministrador().getEmail().equals(email)) throw new AccessDeniedException("Sem permissão.");
        return l;
    }

    /** O lembrete acontece nesta data? */
    public boolean ocorreEm(Lembrete l, LocalDate d) {
        if (d.isBefore(l.getData())) return false;
        return switch (l.getRecorrencia()) {
            case "Diariamente" -> true;
            case "Semanalmente" -> (l.getDiasLista().isEmpty() ? List.of(COD[l.getData().getDayOfWeek().getValue() - 1]) : l.getDiasLista())
                    .contains(COD[d.getDayOfWeek().getValue() - 1]);
            default -> d.equals(l.getData());
        };
    }

    /** Todas as ocorrências entre duas datas (inclusive), ordenadas por data e horário. */
    public List<OcorrenciaLembrete> ocorrencias(PessoaAcompanhada p, LocalDate de, LocalDate ate) {
        List<Lembrete> ls = lembretes.findByPessoa(p);
        Map<String, ConfirmacaoLembrete> conf = new HashMap<>();
        confirmacoes.findByLembretePessoaAndDataBetween(p, de, ate).forEach(c -> conf.put(c.getLembrete().getId() + "|" + c.getData(), c));
        LocalDateTime agora = LocalDateTime.now();
        List<OcorrenciaLembrete> out = new ArrayList<>();
        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1))
            for (Lembrete l : ls) {
                if (!ocorreEm(l, d)) continue;
                ConfirmacaoLembrete c = conf.get(l.getId() + "|" + d);
                if (c != null) out.add(new OcorrenciaLembrete(l, d, "Confirmado", "Resposta às " + String.format("%02dh%02d", c.getMomento().getHour(), c.getMomento().getMinute())));
                else if (d.atTime(l.getHorario()).isAfter(agora)) out.add(new OcorrenciaLembrete(l, d, "Agendado", "Ainda não exibido"));
                else out.add(new OcorrenciaLembrete(l, d, "Não confirmado", "Sem resposta até " + String.format("%02dh%02d", agora.getHour(), agora.getMinute())));
            }
        out.sort(Comparator.comparing(OcorrenciaLembrete::getData).thenComparing(o -> o.getHorarioTexto()));
        return out;
    }

    @Transactional
    public void salvar(PessoaAcompanhada p, String email, LembreteForm f) {
        Lembrete l = f.id() == null ? new Lembrete() : doUsuario(f.id(), email);
        l.setPessoa(p); l.setTitulo(f.titulo().trim()); l.setCategoria(f.categoria());
        l.setDescricao(f.descricao()); l.setData(f.data()); l.setHorario(f.horario()); l.setRecorrencia(f.recorrencia() == null ? "Não se repete" : f.recorrencia());
        l.setDiasSemana(l.getRecorrencia().equals("Semanalmente") && f.dias() != null ? String.join(",", f.dias()) : null);
        l.setMensagem(f.mensagem()); l.setNotificar(Boolean.TRUE.equals(f.notificar()));
        lembretes.save(l);
        ativ.save(new Atividade(LocalDateTime.now(), "Lembrete " + (f.id() == null ? "criado" : "atualizado") + ": " + l.getTitulo(), "Ação do responsável.", "LEMBRETE", p.getImovel()));
    }

    @Transactional
    public void excluir(Long id, String email) {
        Lembrete l = doUsuario(id, email);
        confirmacoes.deleteByLembrete(l);
        lembretes.delete(l);
        ativ.save(new Atividade(LocalDateTime.now(), "Lembrete excluído: " + l.getTitulo(), "Ação do responsável.", "LEMBRETE", l.getPessoa().getImovel()));
    }

    /** Registra a confirmação do dia (na versão final, quem confirma é a pessoa acompanhada). */
    @Transactional
    public void confirmar(Long id, LocalDate data, String email) {
        Lembrete l = doUsuario(id, email);
        if (!ocorreEm(l, data)) throw new IllegalStateException("Este lembrete não ocorre nesta data.");
        if (data.atTime(l.getHorario()).isAfter(LocalDateTime.now())) throw new IllegalStateException("Este lembrete ainda não foi exibido.");
        if (confirmacoes.existsByLembreteAndData(l, data)) throw new IllegalStateException("Este lembrete já foi confirmado.");
        confirmacoes.save(new ConfirmacaoLembrete(l, data, LocalDateTime.now()));
        ativ.save(new Atividade(LocalDateTime.now(), "Lembrete confirmado", l.getPessoa().getNome().split(" ")[0] + " confirmou: " + l.getTitulo() + ".", "LEMBRETE", l.getPessoa().getImovel()));
    }

    /** Cria lembretes de demonstração se a pessoa ainda não tiver nenhum. */
    @Transactional
    public void garantir(PessoaAcompanhada p) {
        if (lembretes.countByPessoa(p) > 0) return;
        LocalDate hoje = LocalDate.now(); LocalDateTime n = LocalDateTime.now(); String nome = p.getNome().split(" ")[0];
        Lembrete med = criar(p, "Lembrete de medicamento", "Medicamento", "Tomar o medicamento da manhã.", hoje, LocalTime.of(8, 0), "Diariamente", nome + ", é hora do seu medicamento.");
        criar(p, "Consulta médica", "Compromisso", "Consulta de rotina.", hoje, LocalTime.of(10, 0), "Não se repete", nome + ", você tem consulta hoje.");
        criar(p, "Horário do almoço", "Alimentação", "Almoço.", hoje, LocalTime.of(12, 0), "Não se repete", nome + ", o almoço está servido.");
        criar(p, "Preparar-se para dormir", "Rotina", "Rotina noturna.", hoje, LocalTime.of(20, 0), "Não se repete", nome + ", é hora de se preparar para dormir.");
        criar(p, "Organizar fotografias", "Atividade cotidiana", "Evento único.", hoje.plusDays(1), LocalTime.of(15, 0), "Não se repete", nome + ", que tal organizar as fotografias?");
        criar(p, "Telefonar para a família", "Compromisso", "Evento único.", hoje.plusDays(3), LocalTime.of(16, 0), "Não se repete", nome + ", hoje é dia de ligar para a família.");
        if (hoje.atTime(8, 5).isBefore(n)) confirmacoes.save(new ConfirmacaoLembrete(med, hoje, hoje.atTime(8, 5)));
    }

    private Lembrete criar(PessoaAcompanhada p, String titulo, String cat, String desc, LocalDate data, LocalTime hora, String rec, String msg) {
        Lembrete l = new Lembrete();
        l.setPessoa(p); l.setTitulo(titulo); l.setCategoria(cat); l.setDescricao(desc); l.setData(data); l.setHorario(hora);
        l.setRecorrencia(rec); l.setMensagem(msg); l.setNotificar(true);
        return lembretes.save(l);
    }
}
