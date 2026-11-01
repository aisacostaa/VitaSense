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
        CATEGORIAS.put("Iluminação", "LUZ"); CATEGORIAS.put("Climatização", "AR"); CATEGORIAS.put("Ventilação", "EXAUSTOR");
        CATEGORIAS.put("Sensor", "SENSOR"); CATEGORIAS.put("Energia", "TOMADA"); CATEGORIAS.put("Segurança", "FECHADURA");
    }
    private final ImovelRepository imoveis; private final DispositivoRepository disp; private final AtividadeRepository ativ;
    public CasaService(ImovelRepository i, DispositivoRepository d, AtividadeRepository a) { imoveis = i; disp = d; ativ = a; }

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

    /** Liga/desliga se estiver conectado e o estado for diferente. Retorna 1 se mudou. */
    private int mudar(Dispositivo d, boolean ligar) {
        if (!d.isConectado() || d.isLigado() == ligar) return 0;
        d.setLigado(ligar); d.setUltimaAtualizacao(LocalDateTime.now()); disp.save(d); return 1;
    }
    private void log(Imovel im, String titulo, String desc) {
        ativ.save(new Atividade(LocalDateTime.now(), titulo, desc, "DISPOSITIVO", im));
    }
}
