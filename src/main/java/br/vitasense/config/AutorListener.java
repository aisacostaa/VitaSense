package br.vitasense.config;

import br.vitasense.model.Atividade;
import jakarta.persistence.PrePersist;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Preenche o autor de toda Atividade nova: o usuário logado, ou "Sistema" (ex.: rotinas agendadas). */
public class AutorListener {
    @PrePersist
    public void definirAutor(Object o) {
        if (o instanceof Atividade a && a.getAutor() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean logado = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
            a.setAutor(logado ? auth.getName() : "Sistema");
        }
    }
}
