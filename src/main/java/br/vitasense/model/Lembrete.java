package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Entity
public class Lembrete {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, categoria, descricao, recorrencia, diasSemana, mensagem;
    private LocalDate data;
    private LocalTime horario;
    private boolean notificar;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public Lembrete() {}
    public Long getId() { return id; }
    public String getTitulo() { return titulo; } public void setTitulo(String v) { titulo = v; }
    public String getCategoria() { return categoria; } public void setCategoria(String v) { categoria = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public String getRecorrencia() { return recorrencia; } public void setRecorrencia(String v) { recorrencia = v; }
    public String getDiasSemana() { return diasSemana; } public void setDiasSemana(String v) { diasSemana = v; }
    public String getMensagem() { return mensagem; } public void setMensagem(String v) { mensagem = v; }
    public LocalDate getData() { return data; } public void setData(LocalDate v) { data = v; }
    public LocalTime getHorario() { return horario; } public void setHorario(LocalTime v) { horario = v; }
    public boolean isNotificar() { return notificar; } public void setNotificar(boolean v) { notificar = v; }
    public PessoaAcompanhada getPessoa() { return pessoa; } public void setPessoa(PessoaAcompanhada v) { pessoa = v; }

    /** "08h00" */
    public String getHorarioTexto() { return String.format("%02dh%02d", horario.getHour(), horario.getMinute()); }
    /** códigos dos dias marcados (SEG, TER...) */
    public List<String> getDiasLista() {
        return diasSemana == null || diasSemana.isBlank() ? new ArrayList<>() : Arrays.asList(diasSemana.split(","));
    }
}
