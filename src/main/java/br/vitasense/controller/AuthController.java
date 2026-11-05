package br.vitasense.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/")
    public String raiz(Authentication auth) {
        boolean admin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return "redirect:" + (admin ? "/dashboard" : "/idoso");
    }

    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/acesso-negado")
    public String negado(Model m) {
        m.addAttribute("titulo", "Acesso não permitido");
        m.addAttribute("mensagem", "Seu perfil não tem permissão para acessar esta página.");
        return "erro";
    }
}
