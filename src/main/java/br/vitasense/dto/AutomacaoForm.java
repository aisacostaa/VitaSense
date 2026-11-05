package br.vitasense.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalTime;
import java.util.List;

public record AutomacaoForm(
        Long id,
        @NotBlank(message = "Informe o nome da rotina.") @Size(max = 60, message = "Nome com até 60 caracteres.") String nome,
        @Size(max = 200, message = "Descrição com até 200 caracteres.") String descricao,
        @Pattern(regexp = "HORARIO|PRESENCA|AUSENCIA|MANUAL|SENSOR", message = "Escolha o gatilho (etapa 1).") String gatilho,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horario,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioFim,
        @Min(value = 1, message = "Minutos deve ser ao menos 1.") @Max(value = 1440, message = "Minutos deve ser no máximo 1440.") Integer minutos,
        Long sensorId,
        @Pattern(regexp = "LIGAR_LUZ|DESLIGAR_LUZ|LIGAR_AQUECEDOR|ATIVAR_EXAUSTOR|ENVIAR_NOTIFICACAO", message = "Escolha a ação (etapa 2).") String acao,
        Long dispositivoId,
        List<String> dias,
        @Size(max = 300, message = "Condições com até 300 caracteres.") String condicoes) {}
