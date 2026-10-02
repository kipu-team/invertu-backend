package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.FrecuenciaAporte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-05 */
@Getter
@Setter
@NoArgsConstructor
public class MetaDetalleResponseDTO {
    private Long idMeta;
    private String nombre;
    private String descripcion;
    private BigDecimal montoObjetivo;
    private BigDecimal montoAportado;           // suma de sus aportes
    private BigDecimal porcentaje;              // montoAportado / montoObjetivo * 100
    private LocalDate fechaObjetivo;
    private FrecuenciaAporte frecuenciaAporte;
    private LocalDate proximaFechaAporte;       // null si es SIN_FRECUENCIA
    private EstadoMeta estado;
    private boolean vencida;                    // ACTIVA con fecha objetivo ya pasada
}
