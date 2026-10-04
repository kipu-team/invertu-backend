package com.upc.invertu.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-SUB-03 */

public record SuscripcionResumenResponseDTO(
        Long idSuscripcion,
        String nombreServicio,
        BigDecimal monto,
        String frecuencia,
        LocalDate proximaFechaCobro,
        String estado,
        boolean pagoSinRegistrar
) {}
