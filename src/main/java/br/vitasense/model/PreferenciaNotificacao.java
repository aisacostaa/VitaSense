package br.vitasense.model;

import jakarta.persistence.*;

/** Preferência de notificação de um responsável por categoria. Categoria silenciada não conta no sino/menu. */
@Entity
public class PreferenciaNotificacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Usuario usuario;
    private String categoria;
    private boolean ativada = true, noPainel = true;

    public PreferenciaNotificacao() {}
    public PreferenciaNotificacao(Usuario u, String categoria) { usuario = u; this.categoria = categoria; }
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getCategoria() { return categoria; }
    public boolean isAtivada() { return ativada; } public void setAtivada(boolean v) { ativada = v; }
    public boolean isNoPainel() { return noPainel; } public void setNoPainel(boolean v) { noPainel = v; }
}
