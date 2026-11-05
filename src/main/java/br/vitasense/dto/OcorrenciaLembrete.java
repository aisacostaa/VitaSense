package br.vitasense.dto;

import br.vitasense.model.Lembrete;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Uma ocorrência de um lembrete em uma data específica, com o estado calculado. */
public class OcorrenciaLembrete {
    private final Lembrete lembrete; private final LocalDate data; private final String estado, detalhe;
    public OcorrenciaLembrete(Lembrete l, LocalDate data, String estado, String detalhe) { lembrete = l; this.data = data; this.estado = estado; this.detalhe = detalhe; }
    public Long getId() { return lembrete.getId(); }
    public String getTitulo() { return lembrete.getTitulo(); }
    public String getCategoria() { return lembrete.getCategoria(); }
    public String getRecorrencia() { return lembrete.getRecorrencia(); }
    public String getHorarioTexto() { return lembrete.getHorarioTexto(); }
    public java.time.LocalTime getHorario() { return lembrete.getHorario(); }
    public LocalDate getData() { return data; }
    public String getDataIso() { return data.toString(); }
    public String getDataTexto() { return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    public String getEstado() { return estado; }
    public String getDetalhe() { return detalhe; }
    public boolean isConfirmado() { return estado.equals("Confirmado"); }
    public boolean isNaoConfirmado() { return estado.equals("Não confirmado"); }
    public boolean isAgendado() { return estado.equals("Agendado"); }
}
