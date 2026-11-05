package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Alerta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, descricao, prioridade, categoria, origem, informacoes, orientacao, visualizadoPor, resolvidoPor, resolucao;
    private LocalDateTime ocorrencia, visualizadoEm, resolvidoEm;
    private Boolean visualizado;                     // wrapper: linhas antigas vêm como null
    @Enumerated(EnumType.STRING) private StatusAlerta status;
    @ManyToOne(optional = false) private Imovel imovel;

    public Alerta() {}
    public Alerta(String titulo, String descricao, String prioridade, LocalDateTime ocorrencia, StatusAlerta status, Imovel imovel) {
        this.titulo = titulo; this.descricao = descricao; this.prioridade = prioridade;
        this.ocorrencia = ocorrencia; this.status = status; this.imovel = imovel;
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getOcorrencia() { return ocorrencia; }
    public String getOcorrenciaTexto() { return Datas.curto(ocorrencia); }
    public StatusAlerta getStatus() { return status; } public void setStatus(StatusAlerta s) { status = s; }
    public Imovel getImovel() { return imovel; }
    public String getCategoria() { return categoria; } public void setCategoria(String v) { categoria = v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem = v; }
    public String getInformacoes() { return informacoes; } public void setInformacoes(String v) { informacoes = v; }
    public String getOrientacao() { return orientacao; } public void setOrientacao(String v) { orientacao = v; }
    public boolean isVisualizado() { return Boolean.TRUE.equals(visualizado); } public void setVisualizado(boolean v) { visualizado = v; }
    public String getVisualizadoPor() { return visualizadoPor; } public void setVisualizadoPor(String v) { visualizadoPor = v; }
    public LocalDateTime getVisualizadoEm() { return visualizadoEm; } public void setVisualizadoEm(LocalDateTime v) { visualizadoEm = v; }
    public String getResolvidoPor() { return resolvidoPor; } public void setResolvidoPor(String v) { resolvidoPor = v; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; } public void setResolvidoEm(LocalDateTime v) { resolvidoEm = v; }
    public String getResolucao() { return resolucao; } public void setResolucao(String v) { resolucao = v; }

    // ---- derivados ----
    /** Alta | Média | Baixa (aceita também os textos antigos, como "Prioridade média"). */
    public String getNivel() {
        String p = prioridade == null ? "" : prioridade.toLowerCase();
        return p.contains("alta") ? "Alta" : p.contains("m") && !p.contains("informativa") ? "Média" : "Baixa";
    }
    public String getPrioridade() { return "Prioridade " + getNivel().toLowerCase(); }
    public boolean isAlta() { return "Alta".equals(getNivel()); }
    public boolean isRequerAtencao() { return !"Baixa".equals(getNivel()); }
    public boolean isNovo() { return status == StatusAlerta.NOVO || status == StatusAlerta.PENDENTE; }
    public boolean isResolvido() { return status == StatusAlerta.RESOLVIDO; }
}
