package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-TRX-01: cada fila del historial de movimientos */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoItemResponseDTO {
    private Long idMovimiento;
    private LocalDate fecha;
    private String descripcion;
    private String categoria;
    private TipoMovimiento tipo;
    private Clasificacion clasificacion;
    private BigDecimal monto;
    private MedioPago medioPago;
    private String suscripcion;
    private boolean tieneComprobante;
}
