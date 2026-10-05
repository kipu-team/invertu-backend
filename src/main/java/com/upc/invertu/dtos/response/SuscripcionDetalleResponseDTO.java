package com.upc.invertu.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** END-SUB-04 detalle de una suscripcion con su historial de pagos y la categoria de su ultimo pago */
public record SuscripcionDetalleResponseDTO(
        Long idSuscripcion,
        String nombreServicio,
        String descripcion,
        BigDecimal monto,
        String frecuencia,
        LocalDate proximaFechaCobro,
        String estado,
        Boolean recordatorioActivo,
        Integer diasAnticipacion,           // null si el recordatorio esta desactivado
        Boolean alertaSaldoActiva,
        boolean pagoSinRegistrar,
        CategoriaPagoDTO ultimaCategoria,   // categoria del pago mas reciente segun US-28, o null si no hay pagos
        List<PagoDTO> pagos                 // del mas reciente al mas antiguo
) {
    public record CategoriaPagoDTO(Long idCategoria, String nombre) {}

    public record PagoDTO(LocalDate fecha, BigDecimal monto, String medioPago) {}   // medioPago puede ser null
}