package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class Medicamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome, dose, origem, orientacao;
    private LocalTime horario;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getDose() { return dose; } public void setDose(String v) { dose = v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem = v; }
    public String getOrientacao() { return orientacao; } public void setOrientacao(String v) { orientacao = v; }
    public LocalTime getHorario() { return horario; } public void setHorario(LocalTime v) { horario = v; }
    public PessoaAcompanhada getPessoa() { return pessoa; } public void setPessoa(PessoaAcompanhada v) { pessoa = v; }
}
