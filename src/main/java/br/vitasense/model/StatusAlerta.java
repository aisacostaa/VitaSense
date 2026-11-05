package br.vitasense.model;

import java.util.List;

public enum StatusAlerta {
    PENDENTE("Pendente"), INFORMATIVO("Informativa"),            // legados (telas antigas)
    NOVO("Novo"), EM_ANDAMENTO("Em andamento"), RESOLVIDO("Resolvido");

    /** Alertas que ainda aguardam o início da verificação (usado no contador do menu e no Dashboard). */
    public static final List<StatusAlerta> AGUARDANDO = List.of(PENDENTE, NOVO);

    private final String rotulo;
    StatusAlerta(String r) { rotulo = r; }
    public String getRotulo() { return rotulo; }
}
