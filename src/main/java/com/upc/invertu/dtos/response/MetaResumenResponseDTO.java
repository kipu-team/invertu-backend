package com.upc.invertu.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upc.invertu.entidades.enums.EstadoMeta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-03, END-GOAL-04 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaResumenResponseDTO {
    private Long idMeta;
    private String nombre;
    private BigDecimal montoObjetivo;
    private BigDecimal montoAportado;     // suma de sus aportes

    // Solo END-GOAL-03 (activas)
    private BigDecimal porcentaje;        // montoAportado / montoObjetivo * 100
    private LocalDate fechaObjetivo;
    private Boolean vencida;              // ACTIVA con fecha objetivo ya pasada

    // Solo END-GOAL-04 (finalizadas)
    private EstadoMeta estado;            // CUMPLIDA o CANCELADA
    private LocalDate fechaCumplimiento;
}
