package br.vitasense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViPaginaController {

    @GetMapping("/idoso/vi")
    public String paginaVi() {
        return "vi";
    }
}