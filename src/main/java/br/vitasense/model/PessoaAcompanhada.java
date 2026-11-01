package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PessoaAcompanhada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome, condicoesSaude, mobilidade;
    private LocalDate nascimento;
    private boolean moraSozinho, usaCelular;
    private String contatoNome, contatoParentesco, contatoTelefone;
    @ManyToOne(optional = false)
    private Imovel imovel;

    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getCondicoesSaude() { return condicoesSaude; } public void setCondicoesSaude(String v) { condicoesSaude = v; }
    public String getMobilidade() { return mobilidade; } public void setMobilidade(String v) { mobilidade = v; }
    public LocalDate getNascimento() { return nascimento; } public void setNascimento(LocalDate v) { nascimento = v; }
    public boolean isMoraSozinho() { return moraSozinho; } public void setMoraSozinho(boolean v) { moraSozinho = v; }
    public boolean isUsaCelular() { return usaCelular; } public void setUsaCelular(boolean v) { usaCelular = v; }
    public String getContatoNome() { return contatoNome; } public void setContatoNome(String v) { contatoNome = v; }
    public String getContatoParentesco() { return contatoParentesco; } public void setContatoParentesco(String v) { contatoParentesco = v; }
    public String getContatoTelefone() { return contatoTelefone; } public void setContatoTelefone(String v) { contatoTelefone = v; }
    public Imovel getImovel() { return imovel; } public void setImovel(Imovel v) { imovel = v; }
}
