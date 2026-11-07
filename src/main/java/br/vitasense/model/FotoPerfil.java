
package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FotoPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String chave;

    private String tipoConteudo;

    @Column(columnDefinition = "BLOB")
    private byte[] dados;

    private LocalDateTime atualizadaEm;

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String v) {
        chave = v;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String v) {
        tipoConteudo = v;
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] v) {
        dados = v;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime v) {
        atualizadaEm = v;
    }
}
