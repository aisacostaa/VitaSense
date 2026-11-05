package br.vitasense.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

/** Formulários da tela Configurações. */
public final class ConfigForms {
    private ConfigForms() {}

    public record Perfil(
            @NotBlank(message = "Informe o nome completo.") @Size(max = 100, message = "Nome com até 100 caracteres.") String nome,
            @NotNull(message = "Informe a data de nascimento.") @Past(message = "Data de nascimento inválida.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nascimento,
            @Pattern(regexp = "|\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos.") String telefone,
            Boolean moraSozinho, Boolean usaCelular,
            @NotBlank(message = "Informe a rua.") String rua,
            @Pattern(regexp = "\\d{1,6}", message = "Número deve conter apenas dígitos.") String numero,
            String complemento,
            @NotBlank(message = "Informe o bairro.") String bairro,
            @NotBlank(message = "Informe a cidade.") String cidade,
            @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos.") String cep) {}

    public record Habitos(
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaDormir,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaAcordar,
            @Size(max = 300, message = "Restrições com até 300 caracteres.") String restricoes,
            @Size(max = 200, message = "Origem com até 200 caracteres.") String origem) {}

    public record Info(
            @Size(max = 1000, message = "Observações com até 1000 caracteres.") String observacoes,
            @Size(max = 200, message = "Origem com até 200 caracteres.") String origem,
            @Size(max = 200, message = "Autorização com até 200 caracteres.") String autorizacao,
            Boolean registroAutorizado) {}

    public record Medicamento(
            Long id,
            @NotBlank(message = "Informe o nome do medicamento.") @Size(max = 100, message = "Nome com até 100 caracteres.") String nome,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horario,
            @Size(max = 100, message = "Dose com até 100 caracteres.") String dose,
            @Size(max = 200, message = "Origem com até 200 caracteres.") String origem,
            @Size(max = 300, message = "Orientação com até 300 caracteres.") String orientacao) {}

    public record Contato(
            Long id,
            @NotBlank(message = "Informe o nome do contato.") @Size(max = 100, message = "Nome com até 100 caracteres.") String nome,
            @NotBlank(message = "Informe o grau de relacionamento.") @Size(max = 50, message = "Relacionamento com até 50 caracteres.") String parentesco,
            @Pattern(regexp = "|\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos.") String telefone,
            @Email(message = "E-mail inválido.") @Size(max = 120, message = "E-mail com até 120 caracteres.") String email,
            @Min(value = 1, message = "Prioridade mínima é 1.") @Max(value = 20, message = "Prioridade máxima é 20.") Integer prioridade,
            Boolean ativo) {}

    public record Conta(
            @NotBlank(message = "Informe o nome.") @Size(max = 100, message = "Nome com até 100 caracteres.") String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") String email,
            @Pattern(regexp = "|\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos.") String telefone) {}

    public record Senha(String atual, String nova, String confirmar) {}
}
