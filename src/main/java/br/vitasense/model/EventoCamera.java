package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EventoCamera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String titulo, status, tipo; // tipo: MOVIMENTO | DESCONEXAO
    private LocalDateTime momento;
    @ManyToOne(optional = false) private Camera camera;

    public EventoCamera() {}
    public EventoCamera(Camera camera, LocalDateTime momento, String titulo, String status, String tipo) {
        this.camera = camera; this.momento = momento; this.titulo = titulo; this.status = status; this.tipo = tipo;
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getStatus() { return status; }
    public String getTipo() { return tipo; }
    public LocalDateTime getMomento() { return momento; }
    public Camera getCamera() { return camera; }
}
