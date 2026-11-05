package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Resolve "quem é o usuário, de qual casa e de qual pessoa acompanhada" para QUALQUER perfil (ADMIN ou COMUM). */
@Service
public class ContextoUsuario {
    private final UsuarioRepository usuarios; private final ImovelRepository imoveis; private final PessoaAcompanhadaRepository pessoas;
    public ContextoUsuario(UsuarioRepository u, ImovelRepository i, PessoaAcompanhadaRepository p) { usuarios = u; imoveis = i; pessoas = p; }

    public boolean isAdmin(Authentication a) { return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN")); }

    public Usuario usuario(Authentication a) {
        return usuarios.findByEmail(a.getName()).orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));
    }

    /** ADMIN: a casa que administra. COMUM: a casa da pessoa acompanhada vinculada ao seu acesso. */
    public Imovel imovel(Authentication a) {
        if (isAdmin(a)) return imoveis.findFirstByAdministradorEmail(a.getName()).orElseThrow(() -> new IllegalStateException("Este usuário não tem imóvel cadastrado."));
        return pessoaVinculada(a).getImovel();
    }

    public PessoaAcompanhada pessoa(Authentication a) {
        if (!isAdmin(a)) return pessoaVinculada(a);
        return pessoas.findFirstByImovel(imovel(a)).orElseThrow(() -> new IllegalStateException("Nenhuma pessoa acompanhada cadastrada."));
    }

    private PessoaAcompanhada pessoaVinculada(Authentication a) {
        PessoaAcompanhada p = usuario(a).getPessoa();
        if (p == null) throw new IllegalStateException("Este acesso não está vinculado a uma pessoa acompanhada.");
        if (!p.isAcessoAtivo()) throw new AccessDeniedException("Acesso desativado pelo responsável.");
        return p;
    }
}
