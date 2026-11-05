package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RegistroSono {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private int minutos;
    private LocalDateTime registradoEm;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public RegistroSono() {}
    public RegistroSono(int minutos, LocalDateTime registradoEm, PessoaAcompanhada pessoa) {
        this.minutos = minutos; this.registradoEm = registradoEm; this.pessoa = pessoa;
    }
    public Long getId() { return id; }
    public int getMinutos() { return minutos; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    /** "7h 30min" */
    public String getDuracao() { return (minutos / 60) + "h " + String.format("%02d", minutos % 60) + "min"; }
}
