package br.vitasense.service;

import br.vitasense.dto.DispositivoForm;
import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/** Regras da tela "Minha casa": cadastro de dispositivos e comandos remotos. */
@Service
public class CasaService {
    public static final List<String> COMODOS = List.of("Sala", "Quarto", "Cozinha", "Banheiro", "Corredor");
    public static final Map<String, String> CATEGORIAS = new LinkedHashMap<>();
    static {
        CATEGORIAS.put("Iluminação", "LUZ"); CATEGORIAS.put("Climatização", "AR"); CATEGORIAS.put("Aquecimento", "AQUECEDOR"); CATEGORIAS.put("Ventilação", "EXAUSTOR");
        CATEGORIAS.put("Sensor", "SENSOR"); CATEGORIAS.put("Energia", "TOMADA"); CATEGORIAS.put("Segurança", "FECHADURA");
    }
    private final ImovelRepository imoveis; private final DispositivoRepository disp; private final AtividadeRepository ativ;
    private final AutomacaoRepository automacoes; private final AlertaService alertaSvc;
    public CasaService(ImovelRepository i, DispositivoRepository d, AtividadeRepository a, AutomacaoRepository au, AlertaService al) {
        imoveis = i; disp = d; ativ = a; automacoes = au; alertaSvc = al;
    }

    public Imovel imovel(String email) {
        return imoveis.findFirstByAdministradorEmail(email).orElseThrow(() -> new IllegalStateException("Usuário sem imóvel."));
    }

    /** Busca o dispositivo garantindo que pertence ao imóvel do usuário. */
    public Dispositivo doUsuario(Long id, String email) {
        Dispositivo d = disp.findById(id).orElseThrow(() -> new NoSuchElementException("Dispositivo não encontrado."));
        if (!d.getImovel().getAdministrador().getEmail().equals(email)) throw new AccessDeniedException("Sem permissão.");
        return d;
    }

    @Transactional
    public void salvar(String email, DispositivoForm f) {
        Imovel im = imovel(email);
        boolean novo = f.id() == null;
        Dispositivo d = novo ? new Dispositivo(f.nome().trim(), CATEGORIAS.get(f.categoria()), f.comodo(), "Ligada".equals(f.estadoInicial()), true, im)
                             : doUsuario(f.id(), email);
        if (!novo && !d.getCategoria().equals(f.categoria())) d.setTipo(CATEGORIAS.get(f.categoria()));
        d.setNome(f.nome().trim()); d.setComodo(f.comodo()); d.setOrigem(f.origem()); d.setTipoConexao(f.tipoConexao());
        d.setIdentificacao(f.identificacao() == null || f.identificacao().isBlank() ? null : f.identificacao().trim().toUpperCase());
        d.setUltimaAtualizacao(LocalDateTime.now());
        disp.save(d);
        log(im, d.getNome() + (novo ? " — dispositivo cadastrado" : " — dispositivo atualizado"), "Alterado pelo responsável.");
    }

    @Transactional
    public void remover(Long id, String email) {
        Dispositivo d = doUsuario(id, email);
        if (automacoes.existsByDispositivoOrSensor(d, d))
            throw new IllegalStateException("\"" + d.getNome() + "\" é usado por uma automação. Edite ou exclua a rotina antes de remover o dispositivo.");
        disp.delete(d);
        log(d.getImovel(), d.getNome() + " — dispositivo removido", "Removido pelo responsável.");
    }

    // ---- controle remoto: cada comando devolve quantos dispositivos mudaram ----
    @Transactional
    public int apagarLuzes(String email) {
        Imovel im = imovel(email);
        int n = 0; for (Dispositivo d : disp.findByImovelOrderByIdAsc(im)) if (d.getTipo().equals("LUZ")) n += mudar(d, false);
        log(im, "Todas as luzes apagadas", n + " luz(es) desligada(s) pelo controle remoto."); return n;
    }
    @Transactional
    public int acenderCorredor(String email) {
        Imovel im = imovel(email);
        int n = 0; for (Dispositivo d : disp.findByImovelOrderByIdAsc(im)) if (d.getTipo().equals("LUZ") && "Corredor".equals(d.getComodo())) n += mudar(d, true);
        log(im, "Luzes do corredor acesas", n + " luz(es) ligada(s) pelo controle remoto."); return n;
    }
    @Transactional
    public int rotinaNoturna(String email) {
        Imovel im = imovel(email);
        int n = 0;
        for (Dispositivo d : disp.findByImovelOrderByIdAsc(im)) {
            if (d.getTipo().equals("LUZ")) n += mudar(d, "Corredor".equals(d.getComodo()));   // apaga luzes, deixa a do corredor
            else if (d.getTipo().equals("AR") && "Quarto".equals(d.getComodo())) n += mudar(d, true);
        }
        log(im, "Rotina noturna ativada", n + " dispositivo(s) ajustado(s)."); return n;
    }
    @Transactional
    public int desativar(List<Long> ids, String email) {
        Imovel im = imovel(email);
        int n = 0; for (Long id : ids == null ? List.<Long>of() : ids) n += mudar(doUsuario(id, email), false);
        if (n > 0) log(im, "Dispositivos desativados", n + " dispositivo(s) desligado(s) pelo responsável.");
        return n;
    }

    /** Simula/corrige uma falha. Abre ou fecha o alerta correspondente automaticamente. */
    @Transactional
    public String alternarFalha(Long id, String email) {
        Dispositivo d = doUsuario(id, email);
        String titulo = "Falha no dispositivo: " + d.getNome();
        if (d.isFalha()) {
            d.setFalhaDescricao(null); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d);
            alertaSvc.resolverPorTitulo(d.getImovel(), titulo, "falha corrigida");
            log(d.getImovel(), d.getNome() + " — falha corrigida", "Registrado pelo responsável.");
            return "Falha de " + d.getNome() + " marcada como corrigida e o alerta foi encerrado.";
        }
        d.setFalhaDescricao("Falha simulada"); d.setLigado(false); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d);
        alertaSvc.abrir(d.getImovel(), "Dispositivos", titulo, d.getNome() + " reportou falha. Equipamento desligado por segurança.", "Média", d.getNome() + " — " + d.getComodo(), "Falha registrada na tela Minha casa.");
        log(d.getImovel(), d.getNome() + " — falha registrada", "Registrado pelo responsável.");
        return "Falha registrada em " + d.getNome() + ". Um alerta foi aberto.";
    }

    /** Simula queda/retorno de conexão. Abre ou fecha o alerta correspondente automaticamente. */
    @Transactional
    public String alternarConexao(Long id, String email) {
        Dispositivo d = doUsuario(id, email);
        String titulo = "Dispositivo desconectado: " + d.getNome();
        d.setConectado(!d.isConectado()); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d);
        if (d.isConectado()) {
            alertaSvc.resolverPorTitulo(d.getImovel(), titulo, "conexão restabelecida");
            log(d.getImovel(), d.getNome() + " — reconectado", "Registrado pelo responsável.");
            return d.getNome() + " reconectado. O alerta foi encerrado.";
        }
        alertaSvc.abrir(d.getImovel(), "Dispositivos", titulo, d.getNome() + " deixou de responder. Conexão indisponível.", "Média", d.getNome() + " — " + d.getComodo(), "Queda de conexão registrada.");
        log(d.getImovel(), d.getNome() + " — conexão perdida", "Registrado pelo responsável.");
        return d.getNome() + " ficou sem conexão. Um alerta foi aberto.";
    }

    /** Botão "Atualizar status": renova a última comunicação dos dispositivos conectados. */
    @Transactional
    public String atualizarStatus(String email) {
        List<Dispositivo> todos = disp.findByImovelOrderByIdAsc(imovel(email));
        LocalDateTime n = LocalDateTime.now();
        todos.stream().filter(Dispositivo::isConectado).forEach(d -> { d.setUltimaAtualizacao(n); disp.save(d); });
        return "Status atualizado: " + todos.stream().filter(Dispositivo::isConectado).count() + " de " + todos.size() + " dispositivos conectados.";
    }

    /** Liga/desliga se estiver conectado e o estado for diferente. Retorna 1 se mudou. */
    private int mudar(Dispositivo d, boolean ligar) {
        if (!d.permiteAlterar(ligar) || d.isLigado() == ligar) return 0;
        d.setLigado(ligar); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d); return 1;
    }
    private void log(Imovel im, String titulo, String desc) {
        ativ.save(new Atividade(LocalDateTime.now(), titulo, desc, "DISPOSITIVO", im));
    }
}
