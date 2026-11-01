package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LeituraSaude {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private LocalDateTime momento;
    private int bpm, sistolica, diastolica, oxigenacao;
    @ManyToOne(optional = false) private PessoaAcompanhada pessoa;

    public LeituraSaude() {}
    public LeituraSaude(LocalDateTime m, int bpm, int sis, int dia, int oxi, PessoaAcompanhada p) {
        momento = m; this.bpm = bpm; sistolica = sis; diastolica = dia; oxigenacao = oxi; pessoa = p;
    }
    public Long getId() { return id; }
    public LocalDateTime getMomento() { return momento; }
    public String getMomentoTexto() { return Datas.curto(momento); }
    public int getBpm() { return bpm; }
    public int getSistolica() { return sistolica; }
    public int getDiastolica() { return diastolica; }
    public int getOxigenacao() { return oxigenacao; }
    public PessoaAcompanhada getPessoa() { return pessoa; }
}
