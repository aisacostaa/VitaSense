package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Atividade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, descricao, tipo;
    private LocalDateTime momento;
    @ManyToOne(optional = false) private Imovel imovel;

    public Atividade() {}
    public Atividade(LocalDateTime momento, String titulo, String descricao, String tipo, Imovel imovel) {
        this.momento = momento; this.titulo = titulo; this.descricao = descricao; this.tipo = tipo; this.imovel = imovel;
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getMomento() { return momento; }
    public String getIcone() {
        return switch (tipo) { case "RELOGIO" -> "⌚"; case "LEMBRETE" -> "🔔"; case "ROTINA" -> "🌙"; case "LUZ" -> "💡"; default -> "🔌"; };
    }
}
