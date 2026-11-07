# Área do idoso — VitaSense

Extraia este zip **por cima** do projeto (os caminhos já são os do projeto). Ele pressupõe a base do sistema já instalada
(qualidade-etapas1e2, acesso-por-codigo e configuracoes-e-login). Seus CSS/HTML personalizados (style.css, login.html,
layout.html, dashboard.*) **não** estão aqui.

## A) Telas do idoso (`/idoso/**`)
| Tela | Rota | Arquivos principais |
|---|---|---|
| Início + "Preciso de ajuda" | `/idoso` | idoso.html, idoso.css, idoso.js |
| Minha casa | `/idoso/casa` | idoso-casa.html, idoso-extra.css |
| Lembretes | `/idoso/lembretes` | idoso-lembretes.html/.css/.js |
| Avisos da casa | `/idoso/avisos` | idoso-avisos.html/.css |
| Ajuda (tutoriais) | `/idoso/ajuda` | idoso-ajuda.html/.css/.js, vídeos em `static/video/ajuda/` |
| Vi (voz/texto) | `/idoso/vi` | **ainda não feita** (cai em idoso-em-breve.html) |
Comuns: fragments/idoso.html (menu, data/hora, diálogo do SOS), IdosoController, IdosoService, AjudaService.

## B) Entrada por código (sem e-mail/senha)
AcessoController, AcessoPorCodigoFilter, TentativasAcesso, AcessoIdosoService, acesso.html, cadastro-concluido.html, acesso.css/.js.
(O botão "Entrar com código" do login está em configuracoes-e-login.zip.)

## C) Ajustes no painel do responsável que ESPELHAM o que o idoso faz
- Lembrete/LembreteService/OcorrenciaLembrete + lembretes.html: confirmação e lembretes criados pela pessoa; alerta automático se não confirmar em 30 min.
- Alerta + alertas.html + AlertasController: pedido de ajuda (categoria "Emergência") e "Pessoa acompanhada leu o aviso".
- Dispositivo: estado em linguagem simples para o idoso.

## Testes
`mvn test` — IdosoServiceTest, IdosoCasaTest, IdosoLembretesTest, IdosoAvisosTest, AjudaServiceTest, AcessoIdosoServiceTest.
