package br.vitasense.config;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import br.vitasense.service.DemoDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration
public class DataInitializer {
    /** Cria o admin de teste (admin@vitasense.com / admin123) com uma casa demo completa. */
    @Bean
    CommandLineRunner seed(UsuarioRepository repo, PasswordEncoder enc, ImovelRepository imoveis,
                           PessoaAcompanhadaRepository pessoas, DemoDataService demo) {
        return args -> {
            Usuario admin = repo.findByEmail("admin@vitasense.com")
                    .orElseGet(() -> repo.save(new Usuario("Carlos Alberto", "admin@vitasense.com", enc.encode("admin123"), Perfil.ADMIN)));
            if (imoveis.findFirstByAdministradorEmail(admin.getEmail()).isPresent()) return;

            Imovel im = new Imovel();
            im.setNome("Residência de Dona Maria"); im.setCep("06900000"); im.setRua("Rua das Flores"); im.setNumero("100");
            im.setBairro("Centro"); im.setCidade("Embu das Artes"); im.setTipo("Casa"); im.setComodos(8); im.setAdministrador(admin);
            imoveis.save(im);

            PessoaAcompanhada p = new PessoaAcompanhada();
            p.setNome("Dona Maria"); p.setNascimento(LocalDate.of(1948, 5, 12)); p.setMoraSozinho(true); p.setUsaCelular(false);
            p.setContatoNome("Carlos Alberto"); p.setContatoParentesco("Filho"); p.setContatoTelefone("11999999999"); p.setImovel(im);
            pessoas.save(p);
            demo.popular(im, p);
        };
    }
}
