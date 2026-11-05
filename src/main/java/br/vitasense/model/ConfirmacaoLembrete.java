package br.vitasense.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Confirmação de UMA ocorrência (lembretes que se repetem são confirmados dia a dia). */
@Entity
public class ConfirmacaoLembrete {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Lembrete lembrete;
    private LocalDate data;
    private LocalDateTime momento;

    public ConfirmacaoLembrete() {}
    public ConfirmacaoLembrete(Lembrete l, LocalDate data, LocalDateTime momento) { lembrete = l; this.data = data; this.momento = momento; }
    public Lembrete getLembrete() { return lembrete; }
    public LocalDate getData() { return data; }
    public LocalDateTime getMomento() { return momento; }
}
