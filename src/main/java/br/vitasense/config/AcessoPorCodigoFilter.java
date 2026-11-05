package br.vitasense.config;

import br.vitasense.model.Usuario;
import br.vitasense.service.AcessoIdosoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

/** POST /acesso: a pessoa acompanhada entra só com o código. Roda DEPOIS do filtro de CSRF (o formulário leva o token). */
public class AcessoPorCodigoFilter extends OncePerRequestFilter {
    private final AcessoIdosoService acesso; private final UsuarioDetailsService detalhes; private final TentativasAcesso tentativas;
    private final HttpSessionSecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    public AcessoPorCodigoFilter(AcessoIdosoService a, UsuarioDetailsService d, TentativasAcesso t) { acesso = a; detalhes = d; tentativas = t; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest r) {
        return !(r.getMethod().equalsIgnoreCase("POST") && r.getRequestURI().equals(r.getContextPath() + "/acesso"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String origem = req.getRemoteAddr(), base = req.getContextPath();
        if (tentativas.bloqueado(origem)) { res.sendRedirect(base + "/acesso?bloqueado"); return; }

        Optional<Usuario> usuario = acesso.autenticar(req.getParameter("codigo"));
        if (usuario.isEmpty()) { tentativas.falha(origem); res.sendRedirect(base + "/acesso?erro"); return; }
        tentativas.sucesso(origem);

        UserDetails ud = detalhes.loadUserByUsername(usuario.get().getEmail());
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(ud, null, ud.getAuthorities());
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(token);
        SecurityContextHolder.setContext(contexto);

        if (req.getSession(false) != null) req.changeSessionId(); else req.getSession(true);   // evita fixação de sessão
        repositorio.saveContext(contexto, req, res);
        acesso.registrarEntrada(usuario.get());
        res.sendRedirect(base + "/idoso");
    }
}
