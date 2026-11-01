package br.vitasense.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class Datas {
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm"), DM = DateTimeFormatter.ofPattern("dd/MM");
    private Datas() {}
    /** "Hoje, 21:12" ou "28/10, 21:12" */
    public static String curto(LocalDateTime t) {
        if (t == null) return "—";
        return (t.toLocalDate().equals(LocalDate.now()) ? "Hoje" : t.format(DM)) + ", " + t.format(HM);
    }
    /** "hoje, às 21:29" ou "28/10, às 21:29" */
    public static String longo(LocalDateTime t) {
        if (t == null) return "—";
        return (t.toLocalDate().equals(LocalDate.now()) ? "hoje" : t.format(DM)) + ", às " + t.format(HM);
    }
}
