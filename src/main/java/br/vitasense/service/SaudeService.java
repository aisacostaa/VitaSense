package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/** Regras da tela Saúde. Sem hardware real, as leituras são simuladas. */
@Service
public class SaudeService {
    private final SmartwatchRepository relogios; private final LeituraSaudeRepository leituras; private final RegistroSonoRepository sonos;
    private final AlertaSaudeRepository alertas; private final AtividadeRepository ativ;

    public SaudeService(SmartwatchRepository r, LeituraSaudeRepository l, RegistroSonoRepository s, AlertaSaudeRepository a, AtividadeRepository at) {
        relogios = r; leituras = l; sonos = s; alertas = a; ativ = at;
    }

    /** Cria os dados de demonstração que faltarem (idempotente; não exige apagar o banco). */
    @Transactional
    public void garantir(PessoaAcompanhada p) {
        LocalDateTime n = LocalDateTime.now();
        if (relogios.findFirstByPessoa(p).isEmpty())
            relogios.save(new Smartwatch("Smartwatch de " + p.getNome(), "Relógio de acompanhamento · VS-024 · Exemplo", true, 76, n.minusMinutes(2), true, p));

        if (leituras.countByPessoa(p) < 20) {                       // histórico das últimas 24h
            int[] bpm = {78,76,80,74,77,79,75}, sis = {120,118,122,119,121,120,117}, dia = {80,78,82,79,80,81,78}, oxi = {98,97,98,99,97,98,98};
            for (int i = 7; i < 48; i++)
                leituras.save(new LeituraSaude(n.minusMinutes(i * 30L + 2), bpm[i % 7], sis[i % 7], dia[i % 7], oxi[i % 7], p));
        }
        if (sonos.findFirstByPessoaOrderByRegistradoEmDesc(p).isEmpty()) {
            int[] min = {450, 420, 395, 480, 430, 455, 410};
            for (int d = 0; d < 7; d++) sonos.save(new RegistroSono(min[d], LocalDate.now().minusDays(d).atTime(6, 30), p));
        }
        if (alertas.countByPessoa(p) == 0) {
            alertas.save(new AlertaSaude("Smartwatch sem sincronização", "Envio de dados interrompido por 15 minutos. A sincronização foi retomada.", n.minusMinutes(50), n.minusMinutes(35), p));
            alertas.save(new AlertaSaude("Dispositivo desconectado", "O smartwatch perdeu a conexão e voltou a ficar conectado em seguida.", n.minusHours(2), n.minusHours(2).plusMinutes(8), p));
            alertas.save(new AlertaSaude("Dados indisponíveis", "Sem leitura de oxigenação por alguns minutos. A lacuna não foi substituída por zero.", n.minusHours(3), n.minusHours(2).minusMinutes(45), p));
            alertas.save(new AlertaSaude("Medição fora do limite configurado", "Exemplo: 102 BPM, acima do limite de acompanhamento de 100 BPM configurado nesta demonstração.", n.minusHours(4), n.minusHours(3).minusMinutes(50), p));
        }
    }

    /** Botão "Atualizar dados": gera uma nova leitura simulada e registra a sincronização. */
    @Transactional
    public void atualizar(PessoaAcompanhada p, Imovel im) {
        garantir(p);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDateTime n = LocalDateTime.now();
        leituras.save(new LeituraSaude(n, r.nextInt(68, 89), r.nextInt(112, 129), r.nextInt(72, 85), r.nextInt(96, 100), p));
        Smartwatch s = relogios.findFirstByPessoa(p).orElseThrow();
        s.setConectado(true); s.setUltimaSincronizacao(n); s.setBateria(Math.max(5, s.getBateria() - 1));
        relogios.save(s);
        ativ.save(new Atividade(n, "Smartwatch sincronizado", "Novas leituras recebidas.", "RELOGIO", im));
    }

    /** Botão "Verificar conexão": registra a verificação e tenta reconectar se estiver sem conexão. */
    @Transactional
    public String verificar(PessoaAcompanhada p) {
        garantir(p);
        Smartwatch s = relogios.findFirstByPessoa(p).orElseThrow();
        s.setUltimaVerificacao(LocalDateTime.now());
        String msg;
        if (s.isConectado()) msg = "Conexão verificada: smartwatch conectado (bateria " + s.getBateria() + "%).";
        else { s.setConectado(true); msg = "O smartwatch estava sem conexão e foi reconectado."; }
        relogios.save(s);
        return msg;
    }
}
