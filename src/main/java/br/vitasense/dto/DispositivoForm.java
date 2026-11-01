package br.vitasense.dto;

import jakarta.validation.constraints.*;

/** Formulário de cadastro/edição de dispositivo (id == null → novo). */
public record DispositivoForm(
        Long id,
        @NotBlank(message = "Informe o nome do dispositivo.") @Size(max = 60, message = "Nome com até 60 caracteres.") String nome,
        @NotBlank(message = "Selecione a categoria.") String categoria,
        @NotBlank(message = "Selecione o cômodo.") String comodo,
        @Pattern(regexp = "Real|Simulado", message = "Origem inválida.") String origem,
        @NotBlank(message = "Selecione o tipo de conexão.") String tipoConexao,
        String estadoInicial,
        @Pattern(regexp = "[A-Za-z0-9-]{0,30}", message = "Identificação aceita só letras, números e hífen.") String identificacao) {}
