package br.vitasense.util;

public final class Formatos {
    private Formatos() {}
    /** "11999998888" -> "(11) 99999-8888"; vazio -> "Não informado". */
    public static String telefone(String bruto) {
        if (bruto == null || bruto.isBlank()) return "Não informado";
        String d = bruto.replaceAll("\\D", "");
        if (d.length() == 11) return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        if (d.length() == 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        return bruto;
    }
}
