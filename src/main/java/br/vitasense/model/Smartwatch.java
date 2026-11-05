package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Smartwatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome, descricao;
    private boolean conectado, coletaAutomatica;
    private int bateria;
    private LocalDateTime ultimaVerificacao;
    private LocalDateTime ultimaSincronizacao;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public Smartwatch() {}
    public Smartwatch(String nome, String descricao, boolean conectado, int bateria, LocalDateTime sync, boolean auto, PessoaAcompanhada p) {
        this.nome = nome; this.descricao = descricao; this.conectado = conectado; this.bateria = bateria;
        ultimaSincronizacao = sync; coletaAutomatica = auto; pessoa = p;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public boolean isConectado() { return conectado; } public void setConectado(boolean v) { conectado = v; }
    public boolean isColetaAutomatica() { return coletaAutomatica; }
    public int getBateria() { return bateria; } public void setBateria(int v) { bateria = v; }
    public LocalDateTime getUltimaSincronizacao() { return ultimaSincronizacao; } public void setUltimaSincronizacao(LocalDateTime v) { ultimaSincronizacao = v; }
    public LocalDateTime getUltimaVerificacao() { return ultimaVerificacao; } public void setUltimaVerificacao(LocalDateTime v) { ultimaVerificacao = v; }
    public PessoaAcompanhada getPessoa() { return pessoa; }
}
