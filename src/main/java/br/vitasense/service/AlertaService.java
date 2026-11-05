package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/** Regras da tela Alertas: visualizar, iniciar verificação e resolver. "Visualizado" é independente do estado. */
@Service
public class AlertaService {
    private final AlertaRepository alertas; private final PessoaAcompanhadaRepository pessoas; private final AtividadeRepository ativ;
    public AlertaService(AlertaRepository a, PessoaAcompanhadaRepository p, AtividadeRepository at) { alertas = a; pessoas = p; ativ = at; }

    public Alerta doUsuario(Long id, String email) {
        Alerta a = alertas.findById(id).orElseThrow(() -> new IllegalStateException("Alerta não encontrado."));
        if (!a.getImovel().getAdministrador().getEmail().equals(email)) throw new AccessDeniedException("Sem permissão.");
        return a;
    }

    /** Cria os alertas de demonstração no novo formato (substitui os antigos, que não tinham categoria). */
    @Transactional
    public void garantir(Imovel im) {
        List<Alerta> todos = alertas.findByImovelOrderByOcorrenciaDesc(im);
        if (todos.stream().anyMatch(a -> a.getCategoria() != null)) return;
        alertas.deleteAll(todos);

        String adm = im.getAdministrador().getNome();
        String pes = pessoas.findFirstByImovel(im).map(PessoaAcompanhada::getNome).orElse("a pessoa acompanhada");
        LocalDateTime n = LocalDateTime.now();
        String padrao = "Verifique a situação no local ou entre em contato com " + pes + ". Visualizar não resolve: conclua o registro após verificar.";

        Alerta q = criar(im, "Saúde", "Queda detectada na sala", "Equipamento compatível reportou sinal de possível queda. Ocorrência ainda não confirmada.", "Alta",
                StatusAlerta.NOVO, n.minusMinutes(12), "Dispositivo compatível — demonstração",
                "Sinal de possível queda reportado pelo equipamento. Sem confirmação adicional disponível.",
                "Entre em contato com " + pes + " para verificar a situação. Em uma emergência real, busque ajuda local.\n\nEste sinal, por si só, não confirma uma emergência e não substitui avaliação profissional.");
        q.setVisualizado(true); q.setVisualizadoPor(adm); q.setVisualizadoEm(n.minusMinutes(8)); alertas.save(q);

        criar(im, "Dispositivos", "Dispositivo da cozinha desconectado", "O dispositivo deixou de responder. Conexão ainda indisponível; verificação iniciada pelo responsável.", "Média",
                StatusAlerta.EM_ANDAMENTO, n.minusMinutes(30), "Sensor da cozinha — demonstração",
                "O dispositivo está sem comunicação. Verifique energia e rede Wi-Fi.", padrao);

        Alerta v = criar(im, "Sensores", "Sensor de vazamento detectou água", "Sinal de água reportado pelo sensor. A origem e a extensão ainda precisam ser verificadas.", "Alta",
                StatusAlerta.EM_ANDAMENTO, n.minusMinutes(55), "Sensor de vazamento — demonstração",
                "Sinal de água reportado. Origem e extensão ainda não confirmadas.", padrao);
        v.setVisualizado(true); v.setVisualizadoPor(adm); v.setVisualizadoEm(n.minusMinutes(50)); alertas.save(v);

        Alerta c = criar(im, "Câmeras", "Câmera do corredor offline.", "Conexão restabelecida. " + adm + " revisou o registro e concluiu.", "Baixa",
                StatusAlerta.RESOLVIDO, n.minusMinutes(80), "Câmera do corredor — demonstração",
                "A câmera ficou sem conexão por alguns minutos e voltou a transmitir.", padrao);
        c.setVisualizado(true); c.setVisualizadoPor(adm); c.setVisualizadoEm(n.minusMinutes(70));
        c.setResolvidoPor(adm); c.setResolvidoEm(n.minusMinutes(60)); alertas.save(c);

        criar(im, "Saúde", "Medição de saúde fora do limite configurado.", "Comparação com a configuração do acompanhamento. Requer revisão dos dados; não comprova doença.", "Média",
                StatusAlerta.NOVO, n.minusHours(2), "Smartwatch de " + pes + " — demonstração",
                "Leitura acima do limite configurado nesta demonstração.", padrao);

        historico(im, "Saúde", "Relógio sem sincronização", n.minusHours(30), "Conexão retomada; " + adm + " revisou e concluiu o registro.", adm);
        historico(im, "Sensores", "Bateria baixa no sensor da entrada", n.minusDays(2), "Troca de bateria registrada por " + adm + ".", adm);
        historico(im, "Lembretes", "Lembrete aguardando confirmação", n.minusDays(3), pes + " confirmou o lembrete; registro concluído.", adm);
    }

    /** Abre um alerta AUTOMÁTICO (disparado por outro módulo). Não duplica se já houver um aberto com o mesmo título. */
    @Transactional
    public Alerta abrir(Imovel im, String categoria, String titulo, String descricao, String prioridade, String origem, String informacoes) {
        garantir(im);
        for (Alerta a : alertas.findByImovelOrderByOcorrenciaDesc(im)) if (a.getTitulo().equals(titulo) && !a.isResolvido()) return a;
        String pes = pessoas.findFirstByImovel(im).map(PessoaAcompanhada::getNome).orElse("a pessoa acompanhada");
        return criar(im, categoria, titulo, descricao, prioridade, StatusAlerta.NOVO, LocalDateTime.now(), origem, informacoes,
                "Verifique a situação no local ou entre em contato com " + pes + ". Visualizar não resolve: conclua o registro após verificar.");
    }

    /** Fecha automaticamente os alertas abertos com este título quando a situação se normaliza. */
    @Transactional
    public void resolverPorTitulo(Imovel im, String titulo, String quem) {
        for (Alerta a : alertas.findByImovelOrderByOcorrenciaDesc(im))
            if (a.getTitulo().equals(titulo) && !a.isResolvido()) {
                a.setStatus(StatusAlerta.RESOLVIDO); a.setResolvidoPor(quem); a.setResolvidoEm(LocalDateTime.now());
                a.setResolucao("Situação normalizada: " + quem + "."); alertas.save(a);
            }
    }

    private Alerta criar(Imovel im, String cat, String titulo, String desc, String prio, StatusAlerta st, LocalDateTime quando, String origem, String info, String orient) {
        Alerta a = new Alerta(titulo, desc, prio, quando, st, im);
        a.setCategoria(cat); a.setOrigem(origem); a.setInformacoes(info); a.setOrientacao(orient);
        return alertas.save(a);
    }
    private void historico(Imovel im, String cat, String titulo, LocalDateTime quando, String resolucao, String quem) {
        Alerta a = criar(im, cat, titulo, resolucao, "Baixa", StatusAlerta.RESOLVIDO, quando, "Registro anterior — demonstração", resolucao, "");
        a.setVisualizado(true); a.setVisualizadoPor(quem); a.setVisualizadoEm(quando.plusMinutes(5));
        a.setResolvidoPor(quem); a.setResolvidoEm(quando.plusMinutes(25)); a.setResolucao(resolucao); alertas.save(a);
    }

    @Transactional
    public void visualizar(Alerta a, String nome) {
        if (a.isVisualizado()) return;
        a.setVisualizado(true); a.setVisualizadoPor(nome); a.setVisualizadoEm(LocalDateTime.now()); alertas.save(a);
    }

    @Transactional
    public int visualizarTodos(Imovel im, String nome) {
        int n = 0;
        for (Alerta a : alertas.findByImovelOrderByOcorrenciaDesc(im)) if (!a.isVisualizado()) { visualizar(a, nome); n++; }
        return n;
    }

    @Transactional
    public void iniciar(Alerta a, String nome) {
        if (!a.isNovo()) throw new IllegalStateException("Só é possível iniciar a verificação de um alerta novo.");
        visualizar(a, nome);
        a.setStatus(StatusAlerta.EM_ANDAMENTO); alertas.save(a);
        log(a, "Verificação iniciada: " + a.getTitulo());
    }

    @Transactional
    public void resolver(Alerta a, String nome) {
        if (a.isResolvido()) throw new IllegalStateException("Este alerta já está resolvido.");
        visualizar(a, nome);
        a.setStatus(StatusAlerta.RESOLVIDO); a.setResolvidoPor(nome); a.setResolvidoEm(LocalDateTime.now());
        a.setResolucao(nome + " revisou e concluiu o registro."); alertas.save(a);
        log(a, "Alerta resolvido: " + a.getTitulo());
    }

    private void log(Alerta a, String titulo) {
        ativ.save(new Atividade(LocalDateTime.now(), titulo, "Ação do responsável.", "DISPOSITIVO", a.getImovel()));
    }
}
