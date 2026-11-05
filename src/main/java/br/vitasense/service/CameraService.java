package br.vitasense.service;

import br.vitasense.model.*;
import br.vitasense.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/** Regras da tela Câmeras: atualização, comunicação por áudio e privacidade. */
@Service
public class CameraService {
    private final CameraRepository cameras; private final ControleCameraRepository controles;
    private final EventoCameraRepository eventos; private final AtividadeRepository ativ;

    public CameraService(CameraRepository c, ControleCameraRepository ct, EventoCameraRepository e, AtividadeRepository a) {
        cameras = c; controles = ct; eventos = e; ativ = a;
    }

    /** Cria controle e eventos de demonstração se ainda não existirem (não exige apagar o banco). */
    @Transactional
    public ControleCamera garantir(Imovel im) {
        if (cameras.findByImovelOrderByIdAsc(im).isEmpty()) {           // banco antigo: cria as câmeras de demonstração
            LocalDateTime t = LocalDateTime.now();
            cameras.save(new Camera("Sala", true, t.minusMinutes(1), im));
            cameras.save(new Camera("Cozinha", true, t.minusMinutes(1), im));
            cameras.save(new Camera("Quarto", true, t.minusMinutes(2), im));
            cameras.save(new Camera("Entrada", false, t.minusHours(3), im));
        }
        ControleCamera ctl = controles.findFirstByImovel(im).orElseGet(() -> controles.save(new ControleCamera(im)));
        if (eventos.countByCameraImovel(im) == 0) {
            LocalDateTime n = LocalDateTime.now(); int i = 0;
            for (Camera c : cameras.findByImovelOrderByIdAsc(im)) {
                String nome = c.getNome().toLowerCase();
                if (c.isOnline() && i < 2) eventos.save(new EventoCamera(c, n.minusMinutes(4L * (++i)), "Movimento detectado na " + nome, "Ocorrência informativa", "MOVIMENTO"));
                else if (!c.isOnline()) eventos.save(new EventoCamera(c, n.minusMinutes(20), "Câmera da " + nome + " desconectada", "Permanece desconectada", "DESCONEXAO"));
            }
        }
        return ctl;
    }

    public Camera doUsuario(Long id, String email) {
        Camera c = cameras.findById(id).orElseThrow(() -> new IllegalStateException("Câmera não encontrada."));
        if (!c.getImovel().getAdministrador().getEmail().equals(email)) throw new AccessDeniedException("Sem permissão.");
        return c;
    }

    /** Botão "Atualizar câmeras": renova a última atualização das câmeras conectadas. */
    @Transactional
    public String atualizar(Imovel im) {
        List<Camera> cs = cameras.findByImovelOrderByIdAsc(im);
        LocalDateTime n = LocalDateTime.now();
        cs.stream().filter(Camera::isOnline).forEach(c -> { c.setUltimaAtualizacao(n); cameras.save(c); });
        return "Câmeras atualizadas: " + cs.stream().filter(Camera::isOnline).count() + " de " + cs.size() + " conectadas.";
    }

    /** acao: falar | ouvir | encerrar */
    @Transactional
    public String audio(Camera cam, String acao) {
        ControleCamera ctl = garantir(cam.getImovel());
        if (acao.equals("encerrar")) {
            if (!ctl.isMicrofoneAtivo() && !ctl.isAudioAtivo()) throw new IllegalStateException("Não há comunicação em andamento.");
            ctl.encerrarComunicacao(); controles.save(ctl);
            log(cam, "Comunicação encerrada na câmera " + cam.getNome());
            return "Comunicação encerrada.";
        }
        if (!cam.isOnline()) throw new IllegalStateException("Câmera desconectada.");
        if (!cam.isAudioBidirecional()) throw new IllegalStateException("Esta câmera é somente vídeo.");
        if (!ctl.isAcessoAutorizado()) throw new IllegalStateException("Acesso não autorizado.");
        if (!ctl.isTransmissaoAtiva()) throw new IllegalStateException("Transmissão interrompida.");
        if (!cam.getId().equals(ctl.getCameraAudioId())) { ctl.encerrarComunicacao(); ctl.setCameraAudioId(cam.getId()); } // troca de câmera zera a sessão
        String msg;
        if (acao.equals("falar")) { ctl.setMicrofoneAtivo(true); msg = "Microfone ativo: sua voz está sendo enviada à residência."; log(cam, "Comunicação iniciada na câmera " + cam.getNome()); }
        else if (acao.equals("ouvir")) { ctl.setAudioAtivo(!ctl.isAudioAtivo()); msg = ctl.isAudioAtivo() ? "Áudio do ambiente ativado." : "Áudio do ambiente desativado."; }
        else throw new IllegalStateException("Ação desconhecida.");
        if (!ctl.isMicrofoneAtivo() && !ctl.isAudioAtivo()) ctl.setCameraAudioId(null);
        controles.save(ctl);
        return msg;
    }

    /** Botão "Reconectar câmera": simula uma nova tentativa de conexão (bem-sucedida) e registra o evento. */
    @Transactional
    public String reconectar(Camera cam) {
        if (cam.isOnline()) throw new IllegalStateException("A câmera já está conectada.");
        cam.setOnline(true); cam.setUltimaAtualizacao(LocalDateTime.now()); cameras.save(cam);
        eventos.save(new EventoCamera(cam, LocalDateTime.now(), "Câmera da " + cam.getNome().toLowerCase() + " reconectada", "Conexão restabelecida", "RECONEXAO"));
        log(cam, "Câmera reconectada: " + cam.getNome());
        return "Câmera " + cam.getNome() + " reconectada.";
    }

    @Transactional
    public String alternarAcesso(Imovel im) {
        ControleCamera ctl = garantir(im);
        ctl.setAcessoAutorizado(!ctl.isAcessoAutorizado());
        if (!ctl.isAcessoAutorizado()) ctl.encerrarComunicacao();
        controles.save(ctl);
        return ctl.isAcessoAutorizado() ? "Acesso às câmeras autorizado." : "Acesso às câmeras revogado.";
    }

    @Transactional
    public String alternarTransmissao(Imovel im) {
        ControleCamera ctl = garantir(im);
        ctl.setTransmissaoAtiva(!ctl.isTransmissaoAtiva());
        if (!ctl.isTransmissaoAtiva()) ctl.encerrarComunicacao();
        controles.save(ctl);
        return ctl.isTransmissaoAtiva() ? "Transmissão retomada." : "Transmissão interrompida.";
    }

    private void log(Camera cam, String titulo) {
        ativ.save(new Atividade(LocalDateTime.now(), titulo, "Ação do responsável.", "DISPOSITIVO", cam.getImovel()));
    }
}
