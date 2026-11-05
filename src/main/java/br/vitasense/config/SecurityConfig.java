package br.vitasense.config;

import br.vitasense.service.AcessoIdosoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import java.io.IOException;

/** Matriz de permissões do sistema (ÚNICO lugar onde se decide quem acessa o quê). */
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain filter(HttpSecurity http, AcessoIdosoService acesso, UsuarioDetailsService detalhes,
                               TentativasAcesso tentativas, SessionRegistry registry) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/cadastro", "/cadastro/**", "/acesso", "/error", "/acesso-negado",
                        "/css/**", "/img/**", "/js/**", "/video/**").permitAll()
                // Painel do responsável: somente ADMIN
                .requestMatchers("/dashboard", "/saude/**", "/cameras/**", "/casa/**", "/alertas/**", "/lembretes/**",
                        "/automacoes/**", "/configuracoes/**", "/dispositivos/**").hasRole("ADMIN")
                // Área da pessoa acompanhada: COMUM (e ADMIN, para visualizar)
                .requestMatchers("/idoso/**").hasAnyRole("COMUM", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").usernameParameter("email").successHandler(this::aoEntrar).permitAll())
            .logout(l -> l.logoutSuccessHandler(this::aoSair))
            .sessionManagement(s -> s.sessionConcurrency(c -> c.maximumSessions(-1).sessionRegistry(registry).expiredUrl("/login?logout")))
            .exceptionHandling(e -> e.accessDeniedPage("/acesso-negado"))
            // login da pessoa acompanhada por código (depois do CSRF, antes da autorização)
            .addFilterBefore(new AcessoPorCodigoFilter(acesso, detalhes, tentativas), AuthorizationFilter.class);
        return http.build();
    }

    private static boolean tem(Authentication auth, String papel) {
        return auth != null && auth.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(papel));
    }

    /** Cada perfil começa na sua tela. */
    private void aoEntrar(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        res.sendRedirect(req.getContextPath() + (tem(auth, "ROLE_ADMIN") ? "/dashboard" : "/idoso"));
    }

    /** A pessoa acompanhada volta para a tela do código; o responsável, para o login. */
    private void aoSair(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        res.sendRedirect(req.getContextPath() + (tem(auth, "ROLE_COMUM") ? "/acesso?saiu" : "/login?logout"));
    }
}
