# VitaSense — redesign completo

Copie as pastas `templates/` e `static/` para dentro de `src/main/resources/`, substituindo os arquivos de mesmo nome.

## Arquivos que podem ser APAGADOS depois (não são mais usados)
- `static/css/`: `style.css`, `app.css`, `acesso.css`, `login-codigo.css`, `cadastro.css`, `vitasense-icones-correcao.css`
- `static/js/`: `liquid-glass-menu.js`
- `templates/dashboard_1.html`, `static/css/dashboard_1.css`, `static/js/dashboard_1.js` (duplicatas)
- `templates/fragments/layout.html` (menu antigo `topo`) — só apague depois de confirmar que nenhuma tela ainda o usa.

## Observações
- O menu novo é `fragments/nav.html` (`nav('pagina')`).
- Todos os `th:`, URLs, formulários, `id`s e `data-*` usados pelos scripts foram mantidos.
- `vs-nav.js` (rolagem + avisos flutuantes) está incluído nas telas internas.
- Não foi possível executar o Thymeleaf no ambiente de criação: teste cada tela com dados reais.

## Área da pessoa acompanhada (idoso)
- Telas: `idoso`, `idoso-casa`, `idoso-lembretes`, `idoso-avisos`, `idoso-ajuda`, `idoso-em-breve`.
- CSS: `idoso.css` (base), `idoso-extra.css`, `idoso-ajuda.css`, `idoso-avisos.css`, `idoso-lembretes.css`. Carregar depois de `vitasense.css`.
- O fragmento `fragments/idoso.html` (menu, topo, SOS) NÃO foi alterado: ele ganhou o novo visual apenas pelo CSS, porque os nomes de classe foram mantidos.
- Mantidos os textos grandes (a partir de 19px), alto contraste e botões de toque com 56–68px.
- Apague depois: `style.css`, `idoso-extra.css` antigo (substituído), e qualquer `idoso*.css` antigo que não esteja na lista acima.
