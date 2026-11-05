package br.vitasense.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Início da pessoa acompanhada (provisório: será a "Tela inicial do idoso"). */
@Controller
public class IdosoController extends BaseController {
    @GetMapping("/idoso")
    public String idoso(Authentication auth, Model m) {
        m.addAttribute("pessoa", pessoa(auth));
        return "idoso";
    }
}
