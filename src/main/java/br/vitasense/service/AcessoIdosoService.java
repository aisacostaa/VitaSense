package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/** Código de acesso da pessoa acompanhada: gera, renova, ativa/desativa e autentica. A pessoa entra só com o código. */
@Service
public class AcessoIdosoService {
    private static final Logger log = LoggerFactory.getLogger(AcessoIdosoService.class);
    private final PessoaAcompanhadaRepository pessoas; private final UsuarioRepository usuarios;
    private final PasswordEncoder enc; private final AtividadeRepository ativ;
    private final SecureRandom rnd = new SecureRandom();

    public AcessoIdosoService(PessoaAcompanhadaRepository p, UsuarioRepository u, PasswordEncoder e, AtividadeRepository a) { pessoas = p; usuarios = u; enc = e; ativ = a; }

    /** "1234-5678" -> "12345678" (aceita espaços, hífens e qualquer caractere que não seja número). */
    public static String normalizar(String digitado) { return digitado == null ? "" : digitado.replaceAll("\\D", ""); }
    public static String formatar(String codigo) { return codigo == null || codigo.length() != 8 ? "—" : codigo.substring(0, 4) + "-" + codigo.substring(4); }

    /** Garante que a pessoa tenha código e um acesso (perfil COMUM) vinculado. Idempotente. */
    @Transactional
    public PessoaAcompanhada garantir(PessoaAcompanhada p) {
        if (p.getCodigoAcesso() == null) {
            p.setCodigoAcesso(novoCodigoUnico()); p.setCodigoGeradoEm(LocalDateTime.now()); p.setAcessoAtivo(true);
            p = pessoas.save(p);
            log.info("Código de acesso gerado para {}: {}", p.getNome(), p.getCodigoFormatado());
        }
        final PessoaAcompanhada pessoa = p;
        usuarios.findFirstByPessoa(pessoa).orElseGet(() -> {
            // senha aleatória: este usuário NUNCA entra por e-mail/senha, somente pelo código
            Usuario u = new Usuario(pessoa.getNome(), "idoso-" + pessoa.getId() + "@vitasense.local", enc.encode(UUID.randomUUID().toString()), Perfil.COMUM);
            u.setPessoa(pessoa);
            return usuarios.save(u);
        });
        return p;
    }

    /** Usado no cadastro: devolve o código já formatado para mostrar ao responsável. */
    @Transactional
    public String garantirCodigo(PessoaAcompanhada p) { return garantir(p).getCodigoFormatado(); }

    /** Gera um código novo; o anterior deixa de funcionar na hora. */
    @Transactional
    public PessoaAcompanhada regenerar(PessoaAcompanhada p) {
        p = garantir(p);
        p.setCodigoAcesso(novoCodigoUnico()); p.setCodigoGeradoEm(LocalDateTime.now());
        p = pessoas.save(p);
        ativ.save(new Atividade(LocalDateTime.now(), "Novo código de acesso gerado", "O código anterior deixou de funcionar.", "ROTINA", p.getImovel()));
        return p;
    }

    @Transactional
    public PessoaAcompanhada alternarAtivo(PessoaAcompanhada p) {
        p = garantir(p);
        p.setAcessoAtivo(!p.isAcessoAtivo());
        p = pessoas.save(p);
        ativ.save(new Atividade(LocalDateTime.now(), p.isAcessoAtivo() ? "Acesso da pessoa acompanhada ativado" : "Acesso da pessoa acompanhada desativado", "Alterado pelo responsável.", "ROTINA", p.getImovel()));
        return p;
    }

    /** Valida o código digitado. Vazio = código inexistente, incorreto ou acesso desativado (sem dizer qual, de propósito). */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String digitado) {
        String c = normalizar(digitado);
        if (c.length() != 8) return Optional.empty();
        return pessoas.findByCodigoAcesso(c).filter(PessoaAcompanhada::isAcessoAtivo).flatMap(usuarios::findFirstByPessoa);
    }

    @Transactional
    public void registrarEntrada(Usuario u) {
        PessoaAcompanhada p = u.getPessoa();
        Atividade a = new Atividade(LocalDateTime.now(), p.getNome() + " entrou no sistema", "Acesso por código.", "ROTINA", p.getImovel());
        a.setAutor(p.getNome());
        ativ.save(a);
    }

    private String novoCodigoUnico() {
        for (int i = 0; i < 30; i++) {
            String c = String.format("%08d", rnd.nextInt(100_000_000));
            if (pessoas.findByCodigoAcesso(c).isEmpty()) return c;
        }
        throw new IllegalStateException("Não foi possível gerar um código único. Tente novamente.");
    }
}
