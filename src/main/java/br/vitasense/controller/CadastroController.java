package br.vitasense.controller;

import br.vitasense.dto.CadastroForm;
import br.vitasense.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CadastroController {
    private final CadastroService service;
    public CadastroController(CadastroService s) { service = s; }

    @GetMapping("/cadastro")
    public String form(Model m) { m.addAttribute("form", new CadastroForm()); return "cadastro"; }

    @PostMapping("/cadastro")
    public String salvar(@Valid @ModelAttribute("form") CadastroForm f, BindingResult br, RedirectAttributes ra) {
        if (f.getSenha() != null && !f.getSenha().equals(f.getConfirmarSenha()))
            br.rejectValue("confirmarSenha", "senha.diferente", "As senhas não coincidem.");
        if (f.getEmail() != null && !br.hasFieldErrors("email") && service.emailEmUso(f.getEmail()))
            br.rejectValue("email", "email.uso", "Este e-mail já está cadastrado.");
        if (br.hasErrors()) return "cadastro";
        String codigo = service.cadastrar(f);                              // código de acesso da pessoa acompanhada
        ra.addFlashAttribute("codigoIdoso", codigo);
        ra.addFlashAttribute("nomeIdoso", f.getIdosoNome());
        return "redirect:/cadastro/concluido";
    }

    /** Mostra o código UMA vez logo após o cadastro (depois, fica em Configurações). */
    @GetMapping("/cadastro/concluido")
    public String concluido(Model m) {
        if (!m.containsAttribute("codigoIdoso")) return "redirect:/login";
        return "cadastro-concluido";
    }
}
