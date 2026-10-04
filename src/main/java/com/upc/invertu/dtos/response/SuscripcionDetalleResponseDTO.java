package com.upc.invertu.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** END-SUB-04 */

public record SuscripcionDetalleResponseDTO(
        Long idSuscripcion,
        String nombreServicio,
        String descripcion,
        BigDecimal monto,
        String frecuencia,
        LocalDate proximaFechaCobro,
        String estado,
        LocalDateTime fechaCreacion,
        boolean pagoSinRegistrar,
        List<PagoHistorialDTO> historialPagos
) {
    public record PagoHistorialDTO(LocalDate fecha, BigDecimal monto, String medioPago) {}
}
