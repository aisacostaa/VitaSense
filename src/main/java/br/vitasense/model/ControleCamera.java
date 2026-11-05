package br.vitasense.model;

import jakarta.persistence.*;

/** Estado das câmeras da casa: permissões de privacidade e sessão de áudio (uma por imóvel). */
@Entity
public class ControleCamera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) private Imovel imovel;
    private boolean acessoAutorizado = true, transmissaoAtiva = true, microfoneAtivo, audioAtivo;
    private Long cameraAudioId; // câmera em que a comunicação está ocorrendo

    public ControleCamera() {}
    public ControleCamera(Imovel imovel) { this.imovel = imovel; }
    public Long getId() { return id; }
    public Imovel getImovel() { return imovel; }
    public boolean isAcessoAutorizado() { return acessoAutorizado; } public void setAcessoAutorizado(boolean v) { acessoAutorizado = v; }
    public boolean isTransmissaoAtiva() { return transmissaoAtiva; } public void setTransmissaoAtiva(boolean v) { transmissaoAtiva = v; }
    public boolean isMicrofoneAtivo() { return microfoneAtivo; } public void setMicrofoneAtivo(boolean v) { microfoneAtivo = v; }
    public boolean isAudioAtivo() { return audioAtivo; } public void setAudioAtivo(boolean v) { audioAtivo = v; }
    public Long getCameraAudioId() { return cameraAudioId; } public void setCameraAudioId(Long v) { cameraAudioId = v; }
    public void encerrarComunicacao() { microfoneAtivo = false; audioAtivo = false; cameraAudioId = null; }
}
