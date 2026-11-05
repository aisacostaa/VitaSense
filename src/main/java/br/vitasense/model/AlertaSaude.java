package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AlertaSaude {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, descricao;
    private LocalDateTime ocorrencia, resolvidoEm;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public AlertaSaude() {}
    public AlertaSaude(String titulo, String descricao, LocalDateTime ocorrencia, LocalDateTime resolvidoEm, PessoaAcompanhada pessoa) {
        this.titulo = titulo; this.descricao = descricao; this.ocorrencia = ocorrencia; this.resolvidoEm = resolvidoEm; this.pessoa = pessoa;
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getOcorrencia() { return ocorrencia; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; }
    public boolean isResolvido() { return resolvidoEm != null; }
}
