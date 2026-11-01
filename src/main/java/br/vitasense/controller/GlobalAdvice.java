package br.vitasense.controller;

import br.vitasense.model.Perfil;
import br.vitasense.model.StatusAlerta;
import br.vitasense.repository.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Dados que o topo/menu precisa em TODAS as telas (nome, perfil, nº de alertas). */
@ControllerAdvice
public class GlobalAdvice {
    private final UsuarioRepository usuarios; private final ImovelRepository imoveis; private final AlertaRepository alertas;
    public GlobalAdvice(UsuarioRepository u, ImovelRepository i, AlertaRepository a) { usuarios = u; imoveis = i; alertas = a; }

    @ModelAttribute
    public void comuns(Authentication auth, Model m) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) return;
        usuarios.findByEmail(auth.getName()).ifPresent(u -> {
            String[] p = u.getNome().trim().split("\\s+");
            m.addAttribute("usuarioNome", u.getNome());
            m.addAttribute("usuarioPerfil", u.getPerfil() == Perfil.ADMIN ? "Responsável" : "Usuário");
            m.addAttribute("iniciais", ("" + p[0].charAt(0) + (p.length > 1 ? p[p.length - 1].charAt(0) : "")).toUpperCase());
            imoveis.findFirstByAdministradorEmail(u.getEmail()).ifPresent(im ->
                m.addAttribute("alertasPendentes", alertas.countByImovelAndStatus(im, StatusAlerta.PENDENTE)));
        });
    }
}
