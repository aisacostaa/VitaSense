package br.vitasense.service;

import br.vitasense.dto.AutomacaoForm;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/** Regras das automações: cadastro, validação de coerência, execução (manual e agendada) e histórico. */
@Service
public class AutomacaoService {
    public static final Map<String, String> TIPO_DA_ACAO = Map.of("LIGAR_LUZ", "LUZ", "DESLIGAR_LUZ", "LUZ", "LIGAR_AQUECEDOR", "AQUECEDOR", "ATIVAR_EXAUSTOR", "EXAUSTOR");

    private final AutomacaoRepository repo; private final ExecucaoAutomacaoRepository historico;
    private final DispositivoRepository disp; private final AtividadeRepository ativ;

    public AutomacaoService(AutomacaoRepository r, ExecucaoAutomacaoRepository h, DispositivoRepository d, AtividadeRepository a) { repo = r; historico = h; disp = d; ativ = a; }

    public Automacao doUsuario(Long id, String email) {
        Automacao a = repo.findById(id).orElseThrow(() -> new IllegalStateException("Rotina não encontrada."));
        if (!a.getImovel().getAdministrador().getEmail().equals(email)) throw new AccessDeniedException("Sem permissão.");
        return a;
    }

    /** Valida a coerência entre gatilho, ação e dispositivo. Devolve a lista de erros (vazia = ok). */
    public List<String> validar(Imovel im, AutomacaoForm f) {
        List<String> e = new ArrayList<>();
        if ("HORARIO".equals(f.gatilho()) && f.horario() == null) e.add("Informe o horário de execução (etapa 1).");
        if ("AUSENCIA".equals(f.gatilho()) && f.minutos() == null) e.add("Informe por quantos minutos sem movimento (etapa 1).");
        if (("AUSENCIA".equals(f.gatilho()) || "SENSOR".equals(f.gatilho())) && f.sensorId() == null) e.add("Escolha o sensor do gatilho (etapa 1).");
        if (f.acao() != null && TIPO_DA_ACAO.containsKey(f.acao())) {
            Dispositivo d = f.dispositivoId() == null ? null : disp.findById(f.dispositivoId()).orElse(null);
            if (d == null || !d.getImovel().getId().equals(im.getId())) e.add("Escolha o equipamento da ação (etapa 2).");
            else if (!d.getTipo().equals(TIPO_DA_ACAO.get(f.acao()))) e.add("O equipamento escolhido não combina com a ação selecionada.");
        }
        return e;
    }

    @Transactional
    public void salvar(Imovel im, String email, AutomacaoForm f) {
        Automacao a = f.id() == null ? new Automacao() : doUsuario(f.id(), email);
        if (f.id() == null) { a.setImovel(im); a.setAtiva(true); }
        a.setNome(f.nome().trim()); a.setDescricao(f.descricao()); a.setGatilho(f.gatilho()); a.setAcao(f.acao());
        a.setHorario(f.horario()); a.setHorarioFim("PRESENCA".equals(f.gatilho()) ? f.horarioFim() : null);
        a.setMinutos("AUSENCIA".equals(f.gatilho()) ? f.minutos() : null);
        a.setSensor(f.sensorId() == null ? null : disp.findById(f.sensorId()).orElse(null));
        a.setDispositivo(TIPO_DA_ACAO.containsKey(f.acao()) && f.dispositivoId() != null ? disp.findById(f.dispositivoId()).orElse(null) : null);
        a.setDias(f.dias() == null || f.dias().size() == 7 ? "" : String.join(",", f.dias()));
        a.setCondicoes(f.condicoes());
        repo.save(a);
    }

    @Transactional public boolean alternar(Automacao a) { a.setAtiva(!a.isAtiva()); repo.save(a); return a.isAtiva(); }
    @Transactional public void excluir(Automacao a) { repo.delete(a); }

    /** Executa a rotina agora (botão "Executar agora" ou agendamento). */
    @Transactional
    public ExecucaoAutomacao executar(Automacao a, String origem) {
        LocalDateTime n = LocalDateTime.now();
        boolean ok = false; String titulo, detalhe;
        Dispositivo d = a.getDispositivo();
        if (!a.isAtiva()) { titulo = "Não executada — rotina inativa"; detalhe = "Ative a rotina para permitir a execução."; }
        else if (a.getAcao().equals("ENVIAR_NOTIFICACAO")) { ok = true; titulo = "Executada"; detalhe = "Notificação registrada no painel. (" + origem + ")"; }
        else if (d == null) { titulo = "Não executada — sem equipamento"; detalhe = "A rotina não tem equipamento vinculado."; }
        else if (!d.isConectado()) { titulo = "Não executada — dispositivo sem conexão"; detalhe = d.getNome() + " sem conexão. Aviso ao responsável."; }
        else if (d.isFalha()) { titulo = "Não executada — dispositivo com falha"; detalhe = d.getNome() + ": " + d.getFalhaDescricao() + ". Execução bloqueada por segurança."; }
        else {
            boolean ligar = !a.getAcao().equals("DESLIGAR_LUZ");
            d.setLigado(ligar); d.setUltimaAtualizacao(n); disp.save(d);
            ok = true; titulo = "Executada"; detalhe = d.getNome() + " · " + d.getComodo() + (ligar ? " ligado(a)." : " desligado(a).") + " (" + origem + ")";
        }
        a.setUltimaExecucao(n); repo.save(a);
        ExecucaoAutomacao ex = historico.save(new ExecucaoAutomacao(a.getImovel(), a.getNome(), n, ok, titulo, detalhe));
        ativ.save(new Atividade(n, (ok ? "Rotina executada: " : "Rotina não executada: ") + a.getNome(), detalhe, "ROTINA", a.getImovel()));
        return ex;
    }

    /** Roda a cada minuto e dispara as rotinas por horário que estiverem na hora. */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void executarAgendadas() {
        LocalDateTime n = LocalDateTime.now(); LocalTime agora = n.toLocalTime().withSecond(0).withNano(0);
        for (Automacao a : repo.findByGatilhoAndAtiva("HORARIO", true)) {
            if (a.getHorario() == null || !a.getHorario().withSecond(0).withNano(0).equals(agora) || !a.ocorreEm(n.getDayOfWeek())) continue;
            if (a.getUltimaExecucao() != null && a.getUltimaExecucao().withSecond(0).withNano(0).equals(n.withSecond(0).withNano(0))) continue;
            executar(a, "agendada");
        }
    }

    /** Cria rotinas e histórico de demonstração se a casa ainda não tiver nenhuma. */
    @Transactional
    public void garantir(Imovel im) {
        if (repo.countByImovel(im) > 0) return;
        Map<String, Dispositivo> m = new HashMap<>();
        disp.findByImovelOrderByIdAsc(im).forEach(d -> m.put(d.getNome(), d));
        criar(im, "Rotina da manhã", "Acende a sala para o início do dia.", "HORARIO", LocalTime.of(7, 0), null, null, null, "LIGAR_LUZ", m.get("Luz da sala"), true, "Ligar somente com conexão e estado disponíveis.");
        criar(im, "Rotina noturna", "Acende a luz do quarto para a rotina da noite.", "HORARIO", LocalTime.of(21, 0), null, null, null, "LIGAR_LUZ", m.get("Luz do quarto"), true, "Não altera as luzes essenciais do corredor.");
        criar(im, "Iluminação do corredor", "Acende a luz ao detectar uma passagem.", "PRESENCA", LocalTime.of(18, 0), LocalTime.of(6, 0), null, m.get("Sensor de presença"), "LIGAR_LUZ", m.get("Luz do corredor"), true, "Apenas liga; não apaga a luz durante a passagem.");
        criar(im, "Climatização do ambiente", "Aquecimento manual proposto, ainda não autorizado.", "MANUAL", null, null, null, null, "LIGAR_AQUECEDOR", m.get("Aquecedor do banheiro"), false, "Aguardando revisão de segurança. Execução bloqueada até a ativação.");
        criar(im, "Economia de energia", "Desliga apenas a iluminação não essencial.", "HORARIO", LocalTime.of(23, 0), null, null, null, "DESLIGAR_LUZ", m.get("Luz da cozinha"), true, "Não altera câmeras, sensores, smartwatch nem luzes essenciais do corredor.");

        LocalDateTime n = LocalDateTime.now();
        historico.save(new ExecucaoAutomacao(im, "Iluminação do corredor", n.minusMinutes(40), true, "Executada", "Presença detectada; Luz do corredor · Corredor ligada."));
        historico.save(new ExecucaoAutomacao(im, "Rotina noturna", n.minusHours(2), true, "Executada", "Luz do quarto · Quarto ligada."));
        historico.save(new ExecucaoAutomacao(im, "Rotina da manhã", n.minusHours(14), true, "Executada", "Luz da sala · Sala ligada."));
        historico.save(new ExecucaoAutomacao(im, "Economia de energia", n.minusDays(1).minusHours(1), true, "Executada", "Luz da cozinha · Cozinha desligada."));
        historico.save(new ExecucaoAutomacao(im, "Rotina da manhã", n.minusDays(1).minusHours(14), false, "Não executada — dispositivo sem conexão", "Luz da sala sem conexão. Aviso ao responsável; conexão restabelecida depois."));
    }

    private void criar(Imovel im, String nome, String desc, String gat, LocalTime h, LocalTime hf, Integer min, Dispositivo sensor, String acao, Dispositivo alvo, boolean ativa, String cond) {
        Automacao a = new Automacao();
        a.setImovel(im); a.setNome(nome); a.setDescricao(desc); a.setGatilho(gat); a.setHorario(h); a.setHorarioFim(hf); a.setMinutos(min);
        a.setSensor(sensor); a.setAcao(acao); a.setDispositivo(alvo); a.setAtiva(ativa); a.setDias(""); a.setCondicoes(cond);
        repo.save(a);
    }
}
