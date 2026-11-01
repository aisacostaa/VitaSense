package br.vitasense.controller;

import br.vitasense.dto.CadastroForm;
import br.vitasense.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CadastroController {
    private final CadastroService service;
    public CadastroController(CadastroService s) { service = s; }

    @GetMapping("/cadastro")
    public String form(Model m) { m.addAttribute("form", new CadastroForm()); return "cadastro"; }

    @PostMapping("/cadastro")
    public String salvar(@Valid @ModelAttribute("form") CadastroForm f, BindingResult br) {
        if (f.getSenha() != null && !f.getSenha().equals(f.getConfirmarSenha()))
            br.rejectValue("confirmarSenha", "senha.diferente", "As senhas não coincidem.");
        if (f.getEmail() != null && !br.hasFieldErrors("email") && service.emailEmUso(f.getEmail()))
            br.rejectValue("email", "email.uso", "Este e-mail já está cadastrado.");
        if (br.hasErrors()) return "cadastro";
        service.cadastrar(f);
        return "redirect:/login?cadastrado";
    }
}
