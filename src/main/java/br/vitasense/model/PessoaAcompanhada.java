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

    // ---- acesso da pessoa acompanhada por código (sem e-mail/senha) ----
    private String codigoAcesso;                       // 8 dígitos, sem hífen
    private Boolean acessoAtivo;                        // wrapper: linhas antigas vêm como null (= ativo)
    private java.time.LocalDateTime codigoGeradoEm;
    public String getCodigoAcesso() { return codigoAcesso; } public void setCodigoAcesso(String v) { codigoAcesso = v; }
    public boolean isAcessoAtivo() { return !Boolean.FALSE.equals(acessoAtivo); } public void setAcessoAtivo(boolean v) { acessoAtivo = v; }
    public java.time.LocalDateTime getCodigoGeradoEm() { return codigoGeradoEm; } public void setCodigoGeradoEm(java.time.LocalDateTime v) { codigoGeradoEm = v; }
    /** "1234-5678" */
    public String getCodigoFormatado() { return codigoAcesso == null || codigoAcesso.length() != 8 ? "—" : codigoAcesso.substring(0, 4) + "-" + codigoAcesso.substring(4); }

    // ---- dados da tela Configurações ----
    private String telefone, restricoesAlimentares, origemHabitos, observacoes, origemObservacoes, autorizacaoTexto;
    private java.time.LocalTime horaDormir, horaAcordar;
    private Boolean registroAutorizado;
    public String getTelefone() { return telefone; } public void setTelefone(String v) { telefone = v; }
    public String getRestricoesAlimentares() { return restricoesAlimentares; } public void setRestricoesAlimentares(String v) { restricoesAlimentares = v; }
    public String getOrigemHabitos() { return origemHabitos; } public void setOrigemHabitos(String v) { origemHabitos = v; }
    public String getObservacoes() { return observacoes; } public void setObservacoes(String v) { observacoes = v; }
    public String getOrigemObservacoes() { return origemObservacoes; } public void setOrigemObservacoes(String v) { origemObservacoes = v; }
    public String getAutorizacaoTexto() { return autorizacaoTexto; } public void setAutorizacaoTexto(String v) { autorizacaoTexto = v; }
    public java.time.LocalTime getHoraDormir() { return horaDormir; } public void setHoraDormir(java.time.LocalTime v) { horaDormir = v; }
    public java.time.LocalTime getHoraAcordar() { return horaAcordar; } public void setHoraAcordar(java.time.LocalTime v) { horaAcordar = v; }
    public boolean isRegistroAutorizado() { return Boolean.TRUE.equals(registroAutorizado); } public void setRegistroAutorizado(boolean v) { registroAutorizado = v; }
    public String getTelefoneFormatado() { return br.vitasense.util.Formatos.telefone(telefone); }
}
