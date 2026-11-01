package br.vitasense.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/** Dados das 4 etapas do cadastro (a 4ª é só revisão). */
public class CadastroForm {
    // Etapa 1 - responsável
    @NotBlank(message = "Informe o nome do responsável.") private String nome;
    @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") private String email;
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos.") private String telefone;
    @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres.") private String senha;
    private String confirmarSenha;
    // Etapa 2 - pessoa idosa
    @NotBlank(message = "Informe o nome da pessoa idosa.") private String idosoNome;
    @NotNull(message = "Informe a data de nascimento.") @Past(message = "Data de nascimento inválida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate idosoNascimento;
    @NotNull(message = "Informe se a pessoa mora sozinha.") private Boolean idosoMoraSozinho;
    @NotNull(message = "Informe se a pessoa usa celular.") private Boolean idosoUsaCelular;
    private String idosoCondicoes;
    private String idosoMobilidade;
    @NotBlank(message = "Informe o contato de emergência.") private String contatoNome;
    @NotBlank(message = "Informe o parentesco.") private String contatoParentesco;
    @Pattern(regexp = "\\d{10,11}", message = "Telefone de emergência deve ter 10 ou 11 dígitos.") private String contatoTelefone;
    // Etapa 3 - imóvel
    @NotBlank(message = "Informe o nome do imóvel.") private String imovelNome;
    @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos.") private String cep;
    @NotBlank(message = "Informe a rua.") private String rua;
    @Pattern(regexp = "\\d{1,6}", message = "Número deve conter apenas dígitos.") private String numero;
    private String complemento;
    @NotBlank(message = "Informe o bairro.") private String bairro;
    @NotBlank(message = "Informe a cidade.") private String cidade;
    @NotBlank(message = "Selecione o tipo de residência.") private String tipoResidencia;
    @NotNull(message = "Informe a quantidade de cômodos.") @Min(value = 1, message = "Cômodos deve ser ao menos 1.") @Max(value = 99, message = "Cômodos deve ser no máximo 99.")
    private Integer comodos;

    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getTelefone() { return telefone; } public void setTelefone(String v) { telefone = v; }
    public String getSenha() { return senha; } public void setSenha(String v) { senha = v; }
    public String getConfirmarSenha() { return confirmarSenha; } public void setConfirmarSenha(String v) { confirmarSenha = v; }
    public String getIdosoNome() { return idosoNome; } public void setIdosoNome(String v) { idosoNome = v; }
    public LocalDate getIdosoNascimento() { return idosoNascimento; } public void setIdosoNascimento(LocalDate v) { idosoNascimento = v; }
    public Boolean getIdosoMoraSozinho() { return idosoMoraSozinho; } public void setIdosoMoraSozinho(Boolean v) { idosoMoraSozinho = v; }
    public Boolean getIdosoUsaCelular() { return idosoUsaCelular; } public void setIdosoUsaCelular(Boolean v) { idosoUsaCelular = v; }
    public String getIdosoCondicoes() { return idosoCondicoes; } public void setIdosoCondicoes(String v) { idosoCondicoes = v; }
    public String getIdosoMobilidade() { return idosoMobilidade; } public void setIdosoMobilidade(String v) { idosoMobilidade = v; }
    public String getContatoNome() { return contatoNome; } public void setContatoNome(String v) { contatoNome = v; }
    public String getContatoParentesco() { return contatoParentesco; } public void setContatoParentesco(String v) { contatoParentesco = v; }
    public String getContatoTelefone() { return contatoTelefone; } public void setContatoTelefone(String v) { contatoTelefone = v; }
    public String getImovelNome() { return imovelNome; } public void setImovelNome(String v) { imovelNome = v; }
    public String getCep() { return cep; } public void setCep(String v) { cep = v; }
    public String getRua() { return rua; } public void setRua(String v) { rua = v; }
    public String getNumero() { return numero; } public void setNumero(String v) { numero = v; }
    public String getComplemento() { return complemento; } public void setComplemento(String v) { complemento = v; }
    public String getBairro() { return bairro; } public void setBairro(String v) { bairro = v; }
    public String getCidade() { return cidade; } public void setCidade(String v) { cidade = v; }
    public String getTipoResidencia() { return tipoResidencia; } public void setTipoResidencia(String v) { tipoResidencia = v; }
    public Integer getComodos() { return comodos; } public void setComodos(Integer v) { comodos = v; }
}
