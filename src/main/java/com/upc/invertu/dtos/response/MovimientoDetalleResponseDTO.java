package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-TRX-04 */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoDetalleResponseDTO {
    private Long idMovimiento;
    private TipoMovimiento tipo;
    private Clasificacion clasificacion;
    private String descripcion;
    private LocalDate fecha;
    private BigDecimal monto;
    private String categoria;        // nombre de la categoria
    private MedioPago medioPago;
    private String suscripcion;      // nombre del servicio, o null
    private String urlComprobante;   // enlace temporal al comprobante, o null
}
