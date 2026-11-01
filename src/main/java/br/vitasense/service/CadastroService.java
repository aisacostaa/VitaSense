package br.vitasense.service;

import br.vitasense.dto.CadastroForm;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroService {
    private final UsuarioRepository usuarios;
    private final ImovelRepository imoveis;
    private final PessoaAcompanhadaRepository pessoas;
    private final PasswordEncoder enc;
    private final DemoDataService demo;

    public CadastroService(UsuarioRepository u, ImovelRepository i, PessoaAcompanhadaRepository p, PasswordEncoder e, DemoDataService d) {
        usuarios = u; imoveis = i; pessoas = p; enc = e; demo = d;
    }

    public boolean emailEmUso(String email) { return usuarios.existsByEmail(email.trim().toLowerCase()); }

    /** Salva admin + imóvel + pessoa acompanhada numa única transação. */
    @Transactional
    public void cadastrar(CadastroForm f) {
        Usuario admin = new Usuario(f.getNome().trim(), f.getEmail().trim().toLowerCase(), enc.encode(f.getSenha()), Perfil.ADMIN);
        admin.setTelefone(f.getTelefone());
        usuarios.save(admin);

        Imovel im = new Imovel();
        im.setNome(f.getImovelNome()); im.setCep(f.getCep()); im.setRua(f.getRua()); im.setNumero(f.getNumero());
        im.setComplemento(f.getComplemento()); im.setBairro(f.getBairro()); im.setCidade(f.getCidade());
        im.setTipo(f.getTipoResidencia()); im.setComodos(f.getComodos()); im.setAdministrador(admin);
        imoveis.save(im);

        PessoaAcompanhada p = new PessoaAcompanhada();
        p.setNome(f.getIdosoNome()); p.setNascimento(f.getIdosoNascimento());
        p.setMoraSozinho(f.getIdosoMoraSozinho()); p.setUsaCelular(f.getIdosoUsaCelular());
        p.setCondicoesSaude(f.getIdosoCondicoes()); p.setMobilidade(f.getIdosoMobilidade());
        p.setContatoNome(f.getContatoNome()); p.setContatoParentesco(f.getContatoParentesco());
        p.setContatoTelefone(f.getContatoTelefone()); p.setImovel(im);
        pessoas.save(p);
        demo.popular(im, p); // dados simulados do dashboard
    }
}
