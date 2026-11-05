package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/** Rotina automática: gatilho (quando) + ação (o que fazer) + dispositivo alvo. */
@Entity
public class Automacao {
    public static final List<String> DIAS = List.of("SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM");
    private static final Map<String, String> ROTULO_DIA = Map.of("SEG", "Seg", "TER", "Ter", "QUA", "Qua", "QUI", "Qui", "SEX", "Sex", "SAB", "Sáb", "DOM", "Dom");

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome, descricao, gatilho, acao, dias, condicoes;   // gatilho: HORARIO|PRESENCA|AUSENCIA|MANUAL|SENSOR
    private LocalTime horario, horarioFim;
    private Integer minutos;
    private boolean ativa;
    private LocalDateTime ultimaExecucao;
    @ManyToOne private Dispositivo sensor;
    @ManyToOne private Dispositivo dispositivo;
    @ManyToOne(optional = false) private Imovel imovel;

    public Automacao() {}
    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public String getGatilho() { return gatilho; } public void setGatilho(String v) { gatilho = v; }
    public String getAcao() { return acao; } public void setAcao(String v) { acao = v; }
    public String getDias() { return dias; } public void setDias(String v) { dias = v; }
    public String getCondicoes() { return condicoes; } public void setCondicoes(String v) { condicoes = v; }
    public LocalTime getHorario() { return horario; } public void setHorario(LocalTime v) { horario = v; }
    public LocalTime getHorarioFim() { return horarioFim; } public void setHorarioFim(LocalTime v) { horarioFim = v; }
    public Integer getMinutos() { return minutos; } public void setMinutos(Integer v) { minutos = v; }
    public boolean isAtiva() { return ativa; } public void setAtiva(boolean v) { ativa = v; }
    public LocalDateTime getUltimaExecucao() { return ultimaExecucao; } public void setUltimaExecucao(LocalDateTime v) { ultimaExecucao = v; }
    public Dispositivo getSensor() { return sensor; } public void setSensor(Dispositivo v) { sensor = v; }
    public Dispositivo getDispositivo() { return dispositivo; } public void setDispositivo(Dispositivo v) { dispositivo = v; }
    public Imovel getImovel() { return imovel; } public void setImovel(Imovel v) { imovel = v; }

    // ---- derivados ----
    public List<String> getDiasLista() { return dias == null || dias.isBlank() ? new ArrayList<>() : Arrays.asList(dias.split(",")); }
    public boolean ocorreEm(java.time.DayOfWeek d) { return getDiasLista().isEmpty() || getDiasLista().contains(DIAS.get(d.getValue() - 1)); }
    public String getDiasTexto() {
        List<String> l = getDiasLista();
        return l.isEmpty() || l.size() == 7 ? "Todos os dias" : String.join(", ", l.stream().map(d -> ROTULO_DIA.getOrDefault(d, d)).toList());
    }
    private static String h(LocalTime t) { return t == null ? "—" : String.format("%02d:%02d", t.getHour(), t.getMinute()); }
    public String getHorarioTexto() { return h(horario); }
    public String getHorarioFimTexto() { return h(horarioFim); }
    public String getGatilhoTexto() {
        return switch (gatilho) {
            case "HORARIO" -> getDiasTexto() + ", às " + h(horario);
            case "PRESENCA" -> "Presença detectada · " + getDiasTexto() + (horario != null ? ", " + h(horario) + "–" + h(horarioFim) : "");
            case "AUSENCIA" -> "Ausência de movimento por " + minutos + " min";
            case "SENSOR" -> "Evento de um sensor" + (sensor != null ? " · " + sensor.getNome() : "");
            default -> "Acionamento manual · Sem horário automático";
        };
    }
    public String getAcaoTexto() {
        return switch (acao) { case "LIGAR_LUZ" -> "Ligar luz"; case "DESLIGAR_LUZ" -> "Desligar luz"; case "LIGAR_AQUECEDOR" -> "Ligar aquecedor";
            case "ATIVAR_EXAUSTOR" -> "Ativar exaustor"; case "ENVIAR_NOTIFICACAO" -> "Enviar notificação"; default -> acao; };
    }
    public String getAlvo() { return dispositivo == null ? "Notificação no painel" : dispositivo.getNome() + " · " + dispositivo.getComodo(); }
    public String getUltimaTexto() { return ultimaExecucao == null ? "Nunca executada" : br.vitasense.util.Datas.curto(ultimaExecucao); }
}
