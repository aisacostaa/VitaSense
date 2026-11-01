package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Camera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome;
    private boolean online;
    private LocalDateTime ultimaAtualizacao;
    @ManyToOne(optional = false) private Imovel imovel;

    public Camera() {}
    public Camera(String nome, boolean online, LocalDateTime ultima, Imovel imovel) {
        this.nome = nome; this.online = online; this.ultimaAtualizacao = ultima; this.imovel = imovel;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public boolean isOnline() { return online; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public String getUltimaTexto() { return Datas.longo(ultimaAtualizacao); }
    public Imovel getImovel() { return imovel; }
}
