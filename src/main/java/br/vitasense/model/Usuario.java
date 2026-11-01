package br.vitasense.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    private String telefone;
    @Column(nullable = false)
    private String senha; // BCrypt
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Perfil perfil = Perfil.COMUM;

    public Usuario() {}
    public Usuario(String nome, String email, String senha, Perfil perfil) {
        this.nome = nome; this.email = email; this.senha = senha; this.perfil = perfil;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}
