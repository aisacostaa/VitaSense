package br.vitasense.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Telas ainda não construídas: mostram "em breve". Remover a rota daqui quando a tela real existir. */
@Controller
public class PaginaController {
    @GetMapping({"/saude", "/cameras", "/alertas", "/lembretes", "/automacoes"})
    public String emBreve(HttpServletRequest r, Model m) {
        String p = r.getRequestURI().substring(1);
        m.addAttribute("ativo", p);
        m.addAttribute("titulo", Character.toUpperCase(p.charAt(0)) + p.substring(1));
        return "em-breve";
    }
}
