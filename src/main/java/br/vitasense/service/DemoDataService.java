package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/** Gera dados simulados (não há hardware real): dispositivos, câmeras, alertas, atividades e leituras. */
@Service
public class DemoDataService {
    private final DispositivoRepository disp; private final CameraRepository cams; private final AlertaRepository alertas;
    private final AtividadeRepository ativ; private final AlertaService alertaSvc; private final LeituraSaudeRepository leituras;

    public DemoDataService(DispositivoRepository d, CameraRepository c, AlertaRepository a, AtividadeRepository at, LeituraSaudeRepository l, AlertaService al) {
        disp = d; cams = c; alertas = a; ativ = at; leituras = l; alertaSvc = al;
    }

    public void popular(Imovel im, PessoaAcompanhada p) {
        LocalDateTime n = LocalDateTime.now();
        Object[][] d = {
            {"Luz da sala","LUZ","Sala",true,true,""}, {"Luz do quarto","LUZ","Quarto",false,true,""},
            {"Aquecedor do banheiro","AQUECEDOR","Banheiro",false,true,""}, {"Exaustor","EXAUSTOR","Banheiro",true,true,"Falha no motor"},
            {"Luz da cozinha","LUZ","Cozinha",true,true,""}, {"Ar-condicionado","AR","Quarto",false,true,""},
            {"Tomada inteligente","TOMADA","Cozinha",true,true,""}, {"Sensor de presença","SENSOR","Corredor",true,true,""},
            {"Fechadura digital","FECHADURA","Sala",true,false,""}, {"Sensor da cozinha","SENSOR","Cozinha",false,false,""},
            {"Luz do corredor","LUZ","Corredor",false,true,""}};
        int k = 1;
        for (Object[] x : d) {
            Dispositivo v = new Dispositivo((String) x[0], (String) x[1], (String) x[2], (boolean) x[3], (boolean) x[4], im);
            v.setIdentificacao(String.format("DEMO-%s-%03d", ((String) x[1]).substring(0, 3), k++));
            if (!((String) x[5]).isEmpty()) v.setFalhaDescricao((String) x[5]);
            disp.save(v);
        }

        cams.save(new Camera("Sala", true, n.minusMinutes(1), im));
        cams.save(new Camera("Cozinha", true, n.minusMinutes(1), im));
        cams.save(new Camera("Quarto", true, n.minusMinutes(2), im));
        cams.save(new Camera("Entrada", false, n.minusHours(3), im));

        alertaSvc.garantir(im); // alertas de demonstração

        String nome = p.getNome().split(" ")[0];
        ativ.save(new Atividade(n.minusMinutes(2), "Smartwatch sincronizado", "Últimas leituras recebidas.", "RELOGIO", im));
        ativ.save(new Atividade(n.minusMinutes(15), "Lembrete confirmado", nome + " confirmou o lembrete.", "LEMBRETE", im));
        ativ.save(new Atividade(n.minusMinutes(30), "Rotina noturna executada", "Automação programada concluída.", "ROTINA", im));
        ativ.save(new Atividade(n.minusMinutes(45), "Luz da sala ligada", "Dispositivo permanece ligado.", "LUZ", im));

        ativ.save(new Atividade(n.minusHours(2), "Exaustor — falha no motor identificada", "Equipamento conectado; funcionamento interrompido.", "DISPOSITIVO", im));
        ativ.save(new Atividade(n.minusHours(2).minusMinutes(5), "Exaustor — ventilação em funcionamento", "Operação normal registrada no banheiro.", "DISPOSITIVO", im));
        ativ.save(new Atividade(n.minusHours(2).minusMinutes(26), "Exaustor — conexão estabelecida", "Comunicação de demonstração iniciada.", "DISPOSITIVO", im));

        int[] bpm = {78,76,80,74,77,79,75}, sis = {120,118,122,119,121,120,117}, dia = {80,78,82,79,80,81,78}, oxi = {98,97,98,99,97,98,98};
        for (int i = 0; i < 7; i++)
            leituras.save(new LeituraSaude(n.minusMinutes(i * 30L + 2), bpm[i], sis[i], dia[i], oxi[i], p));
    }
}
