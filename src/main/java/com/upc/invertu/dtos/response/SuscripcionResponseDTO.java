package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** END-SUB-01, 05, 06, 07 */
@Getter
@Setter
@NoArgsConstructor
public class SuscripcionResponseDTO {
    private Long idSuscripcion;
    private String nombreServicio;
    private String descripcion;
    private BigDecimal monto;
    private FrecuenciaSuscripcion frecuencia;
    private LocalDate proximaFechaCobro;
    private Boolean recordatorioActivo;
    private Integer diasAnticipacion;
    private LocalDate ultimaFechaRecordatorio;
    private Boolean alertaSaldoActiva;
    private LocalDate ultimaFechaAlertaSaldo;
    private EstadoSuscripcion estado;
    private LocalDate fechaCancelacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
