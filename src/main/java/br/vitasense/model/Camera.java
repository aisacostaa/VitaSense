package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Camera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome;
    private boolean online;
    private LocalDateTime ultimaAtualizacao;
    private Boolean audio; // null = padrão (todas bidirecionais, exceto "Entrada")
    @ManyToOne(optional = false) private Imovel imovel;

    public Camera() {}
    public Camera(String nome, boolean online, LocalDateTime ultima, Imovel imovel) {
        this.nome = nome; this.online = online; this.ultimaAtualizacao = ultima; this.imovel = imovel;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean v) { online = v; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime v) { ultimaAtualizacao = v; }
    public String getUltimaTexto() { return Datas.longo(ultimaAtualizacao); }
    public Imovel getImovel() { return imovel; }

    public boolean isAudioBidirecional() { return audio != null ? audio : !"Entrada".equalsIgnoreCase(nome); }
    public String getEstadoTexto() { return online ? "Conectada" : "Desconectada"; }
    public String getDetalhe() {
        if (!online) return "Indisponível desde " + (ultimaAtualizacao == null ? "—" : ultimaAtualizacao.format(DateTimeFormatter.ofPattern("HH:mm")));
        return isAudioBidirecional() ? "Áudio bidirecional compatível" : "Somente vídeo";
    }
    /** Nome do arquivo opcional em static/img (ex.: camera-sala.jpg). */
    public String getImagem() {
        String s = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return "camera-" + s + ".jpg";
    }
}
