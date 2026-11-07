package br.vitasense.model;

import br.vitasense.util.Datas;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Dispositivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String nome, tipo, comodo, origem, tipoConexao, identificacao, falhaDescricao;
    private boolean ligado, conectado;
    private LocalDateTime ultimaAtualizacao;
    @ManyToOne(optional = false) private Imovel imovel;

    public Dispositivo() {}
    public Dispositivo(String nome, String tipo, String comodo, boolean ligado, boolean conectado, Imovel imovel) {
        this.nome = nome; this.tipo = tipo; this.comodo = comodo; this.ligado = ligado; this.conectado = conectado; this.imovel = imovel;
        origem = "Simulado"; tipoConexao = "Wi-Fi"; ultimaAtualizacao = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getTipo() { return tipo; } public void setTipo(String v) { tipo = v; }
    public String getComodo() { return comodo; } public void setComodo(String v) { comodo = v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem = v; }
    public String getTipoConexao() { return tipoConexao; } public void setTipoConexao(String v) { tipoConexao = v; }
    public String getIdentificacao() { return identificacao; } public void setIdentificacao(String v) { identificacao = v; }
    public String getFalhaDescricao() { return falhaDescricao; } public void setFalhaDescricao(String v) { falhaDescricao = v; }
    public boolean isLigado() { return ligado; } public void setLigado(boolean v) { ligado = v; }
    public boolean isConectado() { return conectado; } public void setConectado(boolean v) { conectado = v; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; } public void setUltimaAtualizacao(LocalDateTime v) { ultimaAtualizacao = v; }
    public Imovel getImovel() { return imovel; }

    // ---- derivados (não são colunas) ----
    public boolean isFalha() { return falhaDescricao != null && !falhaDescricao.isBlank(); }
    public String getUltimaTexto() { return Datas.curto(ultimaAtualizacao); }
    public String getCategoria() {
        return switch (tipo) { case "LUZ" -> "Iluminação"; case "AR" -> "Climatização"; case "AQUECEDOR" -> "Aquecimento"; case "EXAUSTOR" -> "Ventilação";
            case "SENSOR" -> "Sensor"; case "TOMADA" -> "Energia"; case "FECHADURA" -> "Segurança"; default -> "Outros"; };
    }
    /** Regra única: sem conexão nada muda; com falha só é permitido DESLIGAR. */
    public boolean permiteAlterar(boolean ligar) { return conectado && !(ligar && isFalha()); }
    /** Por que o dispositivo não pode ser ligado (null = pode). */
    public String motivoBloqueio() { return !conectado ? "dispositivo sem conexão" : isFalha() ? "dispositivo com falha (" + falhaDescricao + ")" : null; }
    /** Luz e tomada são femininas ("Ligada"); os demais, masculinos ("Ligado"). */
    public boolean isFeminino() { return "LUZ".equals(tipo) || "TOMADA".equals(tipo); }
    /** Estado para a área do idoso: curto e sem termos técnicos. */
    public String getEstadoSimples() {
        if (!conectado) return "Sem conexão";
        if (isFalha()) return "Com problema";
        return ligado ? (isFeminino() ? "Ligada" : "Ligado") : (isFeminino() ? "Desligada" : "Desligado");
    }
    public String getEstado() { return !conectado ? "Desconhecido" : isFalha() ? falhaDescricao : ligado ? "Ligado" : "Desligado"; }
    public String getEstadoDetalhe() {
        return !conectado ? "Último estado: " + (ligado ? "ligado" : "desligado") : isFalha() ? "Requer verificação" : "Funcionamento normal";
    }
    public String getIcone() {
        return switch (tipo) { case "LUZ" -> "💡"; case "AQUECEDOR" -> "♨️"; case "EXAUSTOR" -> "🌀"; case "AR" -> "❄️";
            case "TOMADA" -> "🔌"; case "SENSOR" -> "📡"; case "FECHADURA" -> "🔒"; default -> "🏠"; };
    }
}
