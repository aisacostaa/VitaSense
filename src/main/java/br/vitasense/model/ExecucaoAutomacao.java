package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Registro do histórico. Guarda o nome da rotina como texto para sobreviver à exclusão dela. */
@Entity
public class ExecucaoAutomacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String rotinaNome, titulo, detalhe;
    private boolean sucesso;
    private LocalDateTime momento;
    @ManyToOne(optional = false) private Imovel imovel;

    public ExecucaoAutomacao() {}
    public ExecucaoAutomacao(Imovel im, String rotina, LocalDateTime momento, boolean sucesso, String titulo, String detalhe) {
        imovel = im; rotinaNome = rotina; this.momento = momento; this.sucesso = sucesso; this.titulo = titulo; this.detalhe = detalhe;
    }
    public Long getId() { return id; }
    public String getRotinaNome() { return rotinaNome; }
    public String getTitulo() { return titulo; }
    public String getDetalhe() { return detalhe; }
    public boolean isSucesso() { return sucesso; }
    public LocalDateTime getMomento() { return momento; }
    public String getMomentoTexto() { return momento.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")); }
}
