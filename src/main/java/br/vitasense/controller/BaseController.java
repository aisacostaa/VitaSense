package br.vitasense.controller;

import br.vitasense.model.Imovel;
import br.vitasense.model.PessoaAcompanhada;
import br.vitasense.service.ContextoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

/** Comportamento comum dos controllers do painel. A permissão por perfil fica em um só lugar: SecurityConfig. */
public abstract class BaseController {
    @Autowired protected ContextoUsuario ctx;

    protected Imovel imovel(Authentication auth) { return ctx.imovel(auth); }
    protected PessoaAcompanhada pessoa(Authentication auth) { return ctx.pessoa(auth); }
}
