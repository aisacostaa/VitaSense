# VitaSense — Contexto para continuar em outra IA
Projeto ExpoTech 2026.2 (Intelligence, 4º sem. CC). Sistema web de monitoramento/cuidado de idosos (Smart Home).
**Stack:** Java 17, Spring Boot 3.3, Spring Security (form login + BCrypt), Thymeleaf, Spring Data JPA, SQLite.
**Requisitos obrigatórios:** modelagem de dados, fluxograma, UX/UI, validação, front+back integrados, login, banco relacional, 2 perfis (ADMIN / COMUM), POO (classes, herança, polimorfismo, encapsulamento), estruturas de dados.
**Design:** azul-marinho #0a1654, fundo #fbfaff, azul claro #d0e4fd, inputs com borda arredondada (pill), fonte Inter. Referências em Figma (JPGs "HF - ...").
**Estrutura:** br.vitasense.{model,repository,config,controller}; (criar service/ quando houver regra de negócio). Templates em resources/templates, CSS em static/css/style.css.
**Rodar:** `mvn spring-boot:run` → http://localhost:8080 | seed: admin@vitasense.com / admin123
## Telas (fazer 1 por vez: front + back)
- [x] 1. Login
- [x] 2. Cadastro (4 etapas: responsável, idoso, imóvel, revisão) — /cadastro; entidades Imovel e PessoaAcompanhada
- [x] 3. Dashboard (dados simulados via DemoDataService; topo/menu em fragments/layout.html; telas futuras: remover rota de PaginaController)
- [x] 4. Minha casa (CasaController/CasaService; /casa só ADMIN; filtros server-side; controle remoto)
- [x] 5. Câmeras (CamerasController/CameraService; ControleCamera, EventoCamera; áudio e privacidade simulados)
- [x] 6. Saúde (SaudeController/SaudeService; Smartwatch, RegistroSono, AlertaSaude; dados simulados criados sob demanda)
- [x] 7. Lembretes (LembretesController/LembreteService; Lembrete + ConfirmacaoLembrete; recorrência calculada, não materializada)
- [x] 8. Alertas e ocorrências (AlertasController/AlertaService; Alerta ganhou categoria, visualizado, resolução; StatusAlerta NOVO/EM_ANDAMENTO/RESOLVIDO)
- [x] 9. Automações (AutomacaoService com @Scheduled a cada minuto; Automacao + ExecucaoAutomacao; PaginaController agora só tem /em-breve)
- [ ] 10. Config. pessoa acompanhada / Responsável
- [ ] 11. Tela inicial do idoso (perfil COMUM)
- [ ] 12. Vi (assistente voz/texto)
Regra: cada tela = Entity + Repository + Service + Controller + template. Manter código curto e comentado.

## Arquitetura (atualizado na rodada de qualidade)
- **Permissões:** ÚNICO lugar = `config/SecurityConfig` (ADMIN: painel; COMUM/ADMIN: `/idoso/**`). Controllers não checam papel.
- **Contexto do usuário:** `service/ContextoUsuario` resolve usuário → casa → pessoa para ADMIN e COMUM. Controllers do painel herdam `BaseController` (`imovel(auth)`, `pessoa(auth)`).
- **Usuario.pessoa:** vínculo do acesso COMUM com a pessoa acompanhada (usar na tela do idoso).
- **Regras dentro das entidades:** `Dispositivo.permiteAlterar/motivoBloqueio` é a ÚNICA regra de ligar/desligar (Dashboard, Casa, rotinas).
- **Alertas automáticos:** `AlertaService.abrir(...)` / `resolverPorTitulo(...)`. Já usado por falha/conexão de dispositivos; plugar saúde (limites), câmeras, lembretes não confirmados, bateria.
- **Auditoria:** `Atividade.autor` preenchido por `config/AutorListener` (usuário logado ou "Sistema").
- **Avisos:** `VsNavInjectionFilter` injeta `vs-nav.js` e entrega flash `ok`/`erro` como toast em qualquer página.
- **SQLite:** pool=1 e open-in-view=false (application.properties). `vitasense.seed=false` desliga o usuário de demonstração.
- **Testes:** `src/test/java/br/vitasense/*` (regras puras: dispositivo, alerta, lembrete, automação). Rodar: `mvn test`.
## Próximos passos sugeridos
Config (pessoa/responsável) → Tela do idoso (COMUM) → Vi → alertas automáticos de saúde/câmera/lembrete → gatilhos de sensor → Strategy para ações de automação → DER/fluxograma/README.

## Acesso da pessoa acompanhada por CÓDIGO (sem e-mail/senha)
- Cadastro gera um código de 8 dígitos (`PessoaAcompanhada.codigoAcesso`, formato `1234-5678`) e um `Usuario` COMUM vinculado (`idoso-<id>@vitasense.local`, senha aleatória inutilizável). `AcessoIdosoService` cuida de gerar/renovar/ativar/autenticar.
- Login: `GET /acesso` (tela) + `POST /acesso` tratado por `AcessoPorCodigoFilter` (depois do CSRF). 5 erros por IP bloqueiam 10 min (`TentativasAcesso`).
- Responsável: `/cadastro/concluido` mostra o código uma vez; `/configuracoes` mostra, renova e desativa. Código antigo para de funcionar ao renovar; acesso desativado derruba a sessão (ContextoUsuario).
- Pós-login: COMUM vai para `/idoso` (provisório — implementar a "Tela inicial do idoso"). Logout do COMUM volta para `/acesso`.

## Configurações (igual ao Figma) — `/configuracoes?aba=pessoa|responsavel`
- **Pessoa acompanhada:** identificação (+foto, edita também o endereço da casa), **acesso por código**, hábitos, medicamentos (CRUD), informações importantes, contatos de emergência (CRUD; prioridade 1 = principal, sempre ativo; `ConfiguracaoService.renumerar` mantém 1..n).
- **Responsável:** conta (foto, nome, e-mail, telefone; trocar e-mail força novo login), notificações por categoria (categoria silenciada some do contador do sino — `GlobalAdvice`), segurança (trocar senha: mín. 8 c/ letras e números; lista/encerra sessões via `SessionRegistry`).
- Edição inline por query: `editar=perfil|habitos|info|conta`, `med=novo|<id>`, `contato=novo|<id>`. Formulários em `dto/ConfigForms`.
- Fotos: `FotoPerfil` (tabela própria, máx. 2 MB, JPG/PNG/WEBP validados pelo conteúdo). `GET /configuracoes/foto/{pessoa|usuario}` sempre do contexto logado.
- **Login:** botão "Entrar com código" → `/acesso` (arquivo `login-codigo.css`).
