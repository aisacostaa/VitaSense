package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Alerta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, descricao, prioridade;
    private LocalDateTime ocorrencia;
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
    public String getPrioridade() { return prioridade; }
    public LocalDateTime getOcorrencia() { return ocorrencia; }
    public String getOcorrenciaTexto() { return Datas.curto(ocorrencia); }
    public StatusAlerta getStatus() { return status; } public void setStatus(StatusAlerta s) { status = s; }
    public Imovel getImovel() { return imovel; }
}
