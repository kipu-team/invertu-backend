package com.upc.invertu.utilidades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formato de montos y fechas para los textos de los correos de recordatorio */
public final class FormatoCorreo {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatoCorreo() {
    }
    public static String soles(BigDecimal monto) {
        return "S/ " + monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }
}