package br.vitasense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/")      public String raiz()  { return "redirect:/dashboard"; }
    @GetMapping("/login") public String login() { return "login"; }
}
