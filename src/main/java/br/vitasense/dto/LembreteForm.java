package br.vitasense.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record LembreteForm(
        Long id,
        @NotBlank(message = "Informe o título do lembrete.") @Size(max = 80, message = "Título com até 80 caracteres.") String titulo,
        @NotBlank(message = "Selecione a categoria.") String categoria,
        @Size(max = 300, message = "Descrição com até 300 caracteres.") String descricao,
        @NotNull(message = "Informe a data.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @NotNull(message = "Informe o horário.") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horario,
        @Pattern(regexp = "Não se repete|Diariamente|Semanalmente", message = "Recorrência inválida.") String recorrencia,
        List<String> dias,
        @Size(max = 300, message = "Mensagem com até 300 caracteres.") String mensagem,
        Boolean notificar) {}
