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
- [ ] 5. Câmeras
- [ ] 6. Saúde
- [ ] 7. Lembretes
- [ ] 8. Alertas e ocorrências
- [ ] 9. Automações
- [ ] 10. Config. pessoa acompanhada / Responsável
- [ ] 11. Tela inicial do idoso (perfil COMUM)
- [ ] 12. Vi (assistente voz/texto)
Regra: cada tela = Entity + Repository + Service + Controller + template. Manter código curto e comentado.
