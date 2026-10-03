package com.dana.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.OptionalDouble;

/** Formatação única do sistema (sempre em pt-BR: vírgula decimal e datas dd/MM/aaaa). */
public final class Formato {

    public static final Locale LOCALE = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE);

    private Formato() {
    }

    /** String.format sempre com o locale brasileiro. */
    public static String texto(String modelo, Object... argumentos) {
        return String.format(LOCALE, modelo, argumentos);
    }

    public static String creditos(BigDecimal valor) {
        return texto("%.2f", valor);
    }

    public static String horas(double horas) {
        return texto("%.1f", horas);
    }

    public static String data(LocalDate data) {
        return data == null ? "-" : data.format(DATA);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? "-" : dataHora.format(DATA_HORA);
    }

    /** Ex.: nota 3 -> "★★★☆☆". */
    public static String estrelas(int nota) {
        return "★".repeat(nota) + "☆".repeat(5 - nota);
    }

    public static String media(OptionalDouble media) {
        return media.isPresent() ? texto("%.1f ★", media.getAsDouble()) : "sem avaliações";
    }
}
