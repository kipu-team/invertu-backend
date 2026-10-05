package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.TipoMovimiento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-TRX-02, END-TRX-05 */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoResponseDTO {
    private Long idMovimiento;
    private TipoMovimiento tipo;
    private String descripcion;
    private LocalDate fecha;
    private BigDecimal monto;
    private String categoria;
    private String suscripcion;
    private boolean tieneComprobante;
}
