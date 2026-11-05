package br.vitasense.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Tela de entrada da pessoa acompanhada (o envio do código é tratado por AcessoPorCodigoFilter). */
@Controller
public class AcessoController {
    @GetMapping("/acesso")
    public String acesso(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return "redirect:" + (admin ? "/dashboard" : "/idoso");
        }
        return "acesso";
    }
}
