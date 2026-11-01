package br.vitasense.model;

public enum StatusAlerta {
    PENDENTE("Pendente"), INFORMATIVO("Informativa"), RESOLVIDO("Resolvido");
    private final String rotulo;
    StatusAlerta(String r) { rotulo = r; }
    public String getRotulo() { return rotulo; }
}
