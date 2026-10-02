package com.upc.invertu.utilidades;

import com.upc.invertu.entidades.enums.FrecuenciaAporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Calculos de metas compartidos por MetaServiceImpl y AporteServiceImpl. */
public final class CalculosMeta {

    private CalculosMeta() {
    }

    /** Progreso = aportado / objetivo * 100, con 2 decimales. */
    public static BigDecimal porcentaje(BigDecimal aportado, BigDecimal objetivo) {
        return aportado.multiply(BigDecimal.valueOf(100))
                .divide(objetivo, 2, RoundingMode.HALF_UP);
    }

    /** Siguiente fecha de aporte desde hoy segun la frecuencia; nunca despues de la fecha objetivo. */
    public static LocalDate proximaFechaAporte(FrecuenciaAporte frecuencia, LocalDate fechaObjetivo) {
        LocalDate hoy = LocalDate.now();
        LocalDate proxima = switch (frecuencia) {
            case DIARIA -> hoy.plusDays(1);
            case SEMANAL -> hoy.plusWeeks(1);
            case MENSUAL -> hoy.plusMonths(1);
            case SIN_FRECUENCIA -> null;
        };
        if (proxima != null && proxima.isAfter(fechaObjetivo)) {
            return fechaObjetivo;
        }
        return proxima;
    }
}