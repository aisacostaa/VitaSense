package br.vitasense.model;

import jakarta.persistence.*;

@Entity
public class Imovel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome, cep, rua, numero, complemento, bairro, cidade, tipo;
    private Integer comodos;
    @ManyToOne(optional = false)
    private Usuario administrador;

    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getCep() { return cep; } public void setCep(String v) { cep = v; }
    public String getRua() { return rua; } public void setRua(String v) { rua = v; }
    public String getNumero() { return numero; } public void setNumero(String v) { numero = v; }
    public String getComplemento() { return complemento; } public void setComplemento(String v) { complemento = v; }
    public String getBairro() { return bairro; } public void setBairro(String v) { bairro = v; }
    public String getCidade() { return cidade; } public void setCidade(String v) { cidade = v; }
    public String getTipo() { return tipo; } public void setTipo(String v) { tipo = v; }
    public Integer getComodos() { return comodos; } public void setComodos(Integer v) { comodos = v; }
    public Usuario getAdministrador() { return administrador; } public void setAdministrador(Usuario v) { administrador = v; }
}
