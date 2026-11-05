package br.vitasense.service;

import br.vitasense.dto.ConfigForms;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Regras da tela Configurações: pessoa acompanhada, contatos, medicamentos, conta, notificações, senha e fotos. */
@Service
public class ConfiguracaoService {
    /** {categoria do alerta, título, descrição} */
    public static final List<String[]> NOTIFICACOES = List.of(
            new String[]{"Saúde", "Notificações de saúde", "Atualizações do acompanhamento de saúde"},
            new String[]{"Câmeras", "Alertas das câmeras", "Eventos das câmeras autorizadas"},
            new String[]{"Dispositivos", "Alertas de dispositivos", "Mudanças no funcionamento dos equipamentos"},
            new String[]{"Sensores", "Alertas de sensores", "Eventos dos sensores da residência"},
            new String[]{"Lembretes", "Lembretes", "Avisos das rotinas e atividades previstas"},
            new String[]{"Conexão", "Notificações de conexão", "Conexão e indisponibilidade dos equipamentos"});
    private static final long MAX_FOTO = 2L * 1024 * 1024;

    private final PessoaAcompanhadaRepository pessoas; private final ImovelRepository imoveis; private final UsuarioRepository usuarios;
    private final MedicamentoRepository medicamentos; private final ContatoEmergenciaRepository contatos;
    private final PreferenciaNotificacaoRepository prefs; private final FotoPerfilRepository fotos; private final PasswordEncoder enc;

    public ConfiguracaoService(PessoaAcompanhadaRepository p, ImovelRepository i, UsuarioRepository u, MedicamentoRepository m,
                               ContatoEmergenciaRepository c, PreferenciaNotificacaoRepository pr, FotoPerfilRepository f, PasswordEncoder e) {
        pessoas = p; imoveis = i; usuarios = u; medicamentos = m; contatos = c; prefs = pr; fotos = f; enc = e;
    }

    // ---------- regras puras (testáveis sem banco) ----------
    public static int idade(LocalDate nascimento, LocalDate hoje) { return nascimento == null ? 0 : Period.between(nascimento, hoje).getYears(); }
    public static boolean senhaValida(String s) { return s != null && s.length() >= 8 && s.matches(".*[A-Za-z].*") && s.matches(".*\\d.*"); }
    /** Prioridades sempre 1..n, sem buracos nem repetições; o contato principal (1º) é sempre ativo. */
    public static void renumerar(List<ContatoEmergencia> lista) {
        for (int i = 0; i < lista.size(); i++) lista.get(i).setPrioridade(i + 1);
        if (!lista.isEmpty()) lista.get(0).setAtivo(true);
    }
    /** Reconhece JPG/PNG/WEBP pelo conteúdo (não confia na extensão). null = formato não aceito. */
    public static String tipoImagem(byte[] b) {
        if (b == null || b.length < 12) return null;
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        return null;
    }
    private static String vazioNulo(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private static IllegalStateException erro(String m) { return new IllegalStateException(m); }

    // ---------- pessoa acompanhada ----------
    /** Cria o contato principal a partir do cadastro, se a pessoa ainda não tiver contatos. */
    @Transactional
    public void garantir(PessoaAcompanhada p) {
        if (!contatos.findByPessoaOrderByPrioridadeAscIdAsc(p).isEmpty()) return;
        ContatoEmergencia c = new ContatoEmergencia();
        c.setPessoa(p); c.setNome(p.getContatoNome() != null ? p.getContatoNome() : "Contato principal");
        c.setParentesco(p.getContatoParentesco() != null ? p.getContatoParentesco() : "Familiar"); c.setTelefone(p.getContatoTelefone());
        Usuario adm = p.getImovel().getAdministrador();
        if (adm != null && adm.getNome().equalsIgnoreCase(c.getNome())) c.setEmail(adm.getEmail());
        c.setPrioridade(1); c.setAtivo(true);
        contatos.save(c);
    }

    @Transactional
    public void salvarPerfil(PessoaAcompanhada p, ConfigForms.Perfil f) {
        p.setNome(f.nome().trim()); p.setNascimento(f.nascimento()); p.setTelefone(vazioNulo(f.telefone()));
        if (f.moraSozinho() != null) p.setMoraSozinho(f.moraSozinho());
        if (f.usaCelular() != null) p.setUsaCelular(f.usaCelular());
        pessoas.save(p);
        Imovel im = p.getImovel();
        im.setRua(f.rua().trim()); im.setNumero(f.numero()); im.setComplemento(vazioNulo(f.complemento()));
        im.setBairro(f.bairro().trim()); im.setCidade(f.cidade().trim()); im.setCep(f.cep());
        imoveis.save(im);
        usuarios.findFirstByPessoa(p).ifPresent(u -> { u.setNome(p.getNome()); usuarios.save(u); });   // acesso por código usa o mesmo nome
    }

    @Transactional
    public void salvarHabitos(PessoaAcompanhada p, ConfigForms.Habitos f) {
        p.setHoraDormir(f.horaDormir()); p.setHoraAcordar(f.horaAcordar());
        p.setRestricoesAlimentares(vazioNulo(f.restricoes())); p.setOrigemHabitos(vazioNulo(f.origem()));
        pessoas.save(p);
    }

    @Transactional
    public void salvarInfo(PessoaAcompanhada p, ConfigForms.Info f) {
        p.setObservacoes(vazioNulo(f.observacoes())); p.setOrigemObservacoes(vazioNulo(f.origem()));
        p.setAutorizacaoTexto(vazioNulo(f.autorizacao())); p.setRegistroAutorizado(Boolean.TRUE.equals(f.registroAutorizado()));
        pessoas.save(p);
    }

    // ---------- medicamentos ----------
    public List<Medicamento> medicamentos(PessoaAcompanhada p) { return medicamentos.findByPessoaOrderByIdAsc(p); }

    private Medicamento doPessoa(PessoaAcompanhada p, Long id) {
        return medicamentos.findById(id).filter(m -> m.getPessoa().getId().equals(p.getId())).orElseThrow(() -> erro("Medicamento não encontrado."));
    }

    @Transactional
    public void salvarMedicamento(PessoaAcompanhada p, ConfigForms.Medicamento f) {
        Medicamento m = f.id() == null ? new Medicamento() : doPessoa(p, f.id());
        m.setPessoa(p); m.setNome(f.nome().trim()); m.setHorario(f.horario()); m.setDose(vazioNulo(f.dose()));
        m.setOrigem(vazioNulo(f.origem())); m.setOrientacao(vazioNulo(f.orientacao()));
        medicamentos.save(m);
    }

    @Transactional public void removerMedicamento(PessoaAcompanhada p, Long id) { medicamentos.delete(doPessoa(p, id)); }

    // ---------- contatos de emergência ----------
    public List<ContatoEmergencia> contatos(PessoaAcompanhada p) { return contatos.findByPessoaOrderByPrioridadeAscIdAsc(p); }

    @Transactional
    public void salvarContato(PessoaAcompanhada p, ConfigForms.Contato f) {
        List<ContatoEmergencia> todos = new ArrayList<>(contatos.findByPessoaOrderByPrioridadeAscIdAsc(p));
        ContatoEmergencia c = f.id() == null ? new ContatoEmergencia()
                : todos.stream().filter(x -> x.getId().equals(f.id())).findFirst().orElseThrow(() -> erro("Contato não encontrado."));
        c.setPessoa(p); c.setNome(f.nome().trim()); c.setParentesco(f.parentesco().trim()); c.setTelefone(vazioNulo(f.telefone()));
        c.setEmail(vazioNulo(f.email())); c.setAtivo(f.ativo() == null || f.ativo());
        todos.remove(c);
        int posicao = f.prioridade() == null ? todos.size() + 1 : Math.max(1, Math.min(f.prioridade(), todos.size() + 1));
        todos.add(posicao - 1, c);                     // quem foi editado ocupa a posição escolhida; os demais se ajustam
        renumerar(todos);
        contatos.saveAll(todos);
    }

    @Transactional
    public void removerContato(PessoaAcompanhada p, Long id) {
        List<ContatoEmergencia> todos = new ArrayList<>(contatos.findByPessoaOrderByPrioridadeAscIdAsc(p));
        if (todos.size() <= 1) throw erro("Mantenha pelo menos um contato de emergência.");
        ContatoEmergencia alvo = todos.stream().filter(x -> x.getId().equals(id)).findFirst().orElseThrow(() -> erro("Contato não encontrado."));
        todos.remove(alvo); contatos.delete(alvo);
        renumerar(todos); contatos.saveAll(todos);     // se era o principal, o próximo assume
    }

    // ---------- conta do responsável ----------
    /** Devolve true se o e-mail mudou (o e-mail é o login: a sessão precisa ser reiniciada). */
    @Transactional
    public boolean salvarConta(Usuario u, ConfigForms.Conta f) {
        String email = f.email().trim().toLowerCase();
        boolean mudou = !email.equals(u.getEmail());
        if (mudou && usuarios.existsByEmail(email)) throw erro("Este e-mail já está em uso.");
        u.setNome(f.nome().trim()); u.setEmail(email); u.setTelefone(vazioNulo(f.telefone()));
        usuarios.save(u);
        return mudou;
    }

    @Transactional
    public void alterarSenha(Usuario u, ConfigForms.Senha f) {
        if (f.atual() == null || !enc.matches(f.atual(), u.getSenha())) throw erro("A senha atual está incorreta.");
        if (!senhaValida(f.nova())) throw erro("A nova senha deve ter pelo menos 8 caracteres, com letras e números.");
        if (!f.nova().equals(f.confirmar())) throw erro("A confirmação não coincide com a nova senha.");
        if (enc.matches(f.nova(), u.getSenha())) throw erro("A nova senha deve ser diferente da atual.");
        u.setSenha(enc.encode(f.nova()));
        usuarios.save(u);
    }

    // ---------- notificações ----------
    public List<Map<String, Object>> preferencias(Usuario u) {
        Map<String, PreferenciaNotificacao> atuais = prefs.findByUsuarioId(u.getId()).stream().collect(Collectors.toMap(PreferenciaNotificacao::getCategoria, Function.identity(), (a, b) -> a));
        List<Map<String, Object>> out = new ArrayList<>();
        for (String[] n : NOTIFICACOES) {
            PreferenciaNotificacao pr = atuais.get(n[0]);
            out.add(Map.of("categoria", n[0], "titulo", n[1], "descricao", n[2], "ativada", pr == null || pr.isAtivada(), "painel", pr == null || pr.isNoPainel()));
        }
        return out;
    }

    @Transactional
    public void salvarPreferencias(Usuario u, boolean[] ativadas, boolean[] painel) {
        Map<String, PreferenciaNotificacao> atuais = prefs.findByUsuarioId(u.getId()).stream().collect(Collectors.toMap(PreferenciaNotificacao::getCategoria, Function.identity(), (a, b) -> a));
        for (int i = 0; i < NOTIFICACOES.size(); i++) {
            String cat = NOTIFICACOES.get(i)[0];
            PreferenciaNotificacao pr = atuais.getOrDefault(cat, new PreferenciaNotificacao(u, cat));
            pr.setAtivada(ativadas[i]); pr.setNoPainel(painel[i]);
            prefs.save(pr);
        }
    }

    /** Categorias que NÃO devem aparecer no contador do sino. */
    public List<String> categoriasSilenciadas(Long usuarioId) {
        return prefs.findByUsuarioId(usuarioId).stream().filter(x -> !x.isAtivada() || !x.isNoPainel()).map(PreferenciaNotificacao::getCategoria).toList();
    }

    // ---------- fotos ----------
    @Transactional
    public void salvarFoto(String chave, MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) throw erro("Escolha uma imagem.");
        if (arquivo.getSize() > MAX_FOTO) throw erro("A imagem deve ter no máximo 2 MB.");
        byte[] dados = arquivo.getBytes();
        String tipo = tipoImagem(dados);
        if (tipo == null) throw erro("Use uma imagem JPG, PNG ou WEBP.");
        FotoPerfil f = fotos.findByChave(chave).orElseGet(FotoPerfil::new);
        f.setChave(chave); f.setTipoConteudo(tipo); f.setDados(dados); f.setAtualizadaEm(LocalDateTime.now());
        fotos.save(f);
    }
    public Optional<FotoPerfil> foto(String chave) { return fotos.findByChave(chave); }
}
