
package br.vitasense.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "vi_memoria",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_vi_memoria_usuario",
        columnNames = "usuario_id"
    )
)
public class ViMemoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "historico", nullable = false, columnDefinition = "TEXT")
    private String historico = "[]";

    public ViMemoria() {
    }

    public ViMemoria(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getHistorico() {
        return historico;
    }

    public void setHistorico(String historico) {
        this.historico = historico;
    }
}
