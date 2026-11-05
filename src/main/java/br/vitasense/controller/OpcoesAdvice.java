package br.vitasense.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Listas fixas usadas em selects/checkboxes dos templates (evita listas literais no HTML). */
@ControllerAdvice
public class OpcoesAdvice {
    @ModelAttribute("recorrencias") public List<String> recorrencias() { return List.of("Não se repete", "Diariamente", "Semanalmente"); }
    @ModelAttribute("diasSemanaCod") public List<String> diasSemanaCod() { return List.of("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"); }
    @ModelAttribute("conexoes") public List<String> conexoes() { return List.of("Online", "Offline"); }
    @ModelAttribute("funcionamentos") public List<String> funcionamentos() { return List.of("Ligado", "Desligado", "Com falha"); }
    @ModelAttribute("tiposConexao") public List<String> tiposConexao() { return List.of("Wi-Fi", "Bluetooth", "Zigbee"); }

    @ModelAttribute("gatilhos")
    public Map<String, String> gatilhos() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("HORARIO", "Horário específico"); m.put("PRESENCA", "Presença detectada");
        m.put("AUSENCIA", "Ausência de movimento por determinado período"); m.put("MANUAL", "Acionamento manual"); m.put("SENSOR", "Evento de um sensor");
        return m;
    }
    @ModelAttribute("acoesAuto")
    public Map<String, String> acoesAuto() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("LIGAR_LUZ", "Ligar luz"); m.put("DESLIGAR_LUZ", "Desligar luz"); m.put("LIGAR_AQUECEDOR", "Ligar aquecedor");
        m.put("ATIVAR_EXAUSTOR", "Ativar exaustor"); m.put("ENVIAR_NOTIFICACAO", "Enviar notificação");
        return m;
    }
}
