package br.vitasense.model;

import jakarta.persistence.*;

/** Contatos de emergência em ordem de prioridade. A prioridade 1 é o contato principal (sempre ativo). */
@Entity
public class ContatoEmergencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome, parentesco, telefone, email;
    private int prioridade;
    private boolean ativo = true;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getParentesco() { return parentesco; } public void setParentesco(String v) { parentesco = v; }
    public String getTelefone() { return telefone; } public void setTelefone(String v) { telefone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public int getPrioridade() { return prioridade; } public void setPrioridade(int v) { prioridade = v; }
    public boolean isAtivo() { return ativo; } public void setAtivo(boolean v) { ativo = v; }
    public PessoaAcompanhada getPessoa() { return pessoa; } public void setPessoa(PessoaAcompanhada v) { pessoa = v; }
    public boolean isPrincipal() { return prioridade == 1; }
    public String getPrioridadeTexto() { return prioridade + "ª prioridade"; }
    public String getTelefoneFormatado() { return br.vitasense.util.Formatos.telefone(telefone); }
}
