package com.upc.invertu.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-SUB-03, cada suscripcion del listado */
public record SuscripcionResumenResponseDTO(
        Long idSuscripcion,
        String nombreServicio,
        BigDecimal monto,
        String frecuencia,
        LocalDate proximaFechaCobro,
        String estado,
        Boolean recordatorioActivo,
        boolean pagoSinRegistrar
) {}