package com.upc.invertu.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upc.invertu.entidades.enums.EstadoMeta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * END-GOAL-06, 12 y 13. Cada endpoint llena solo sus campos los que quedan en null no se envian en el JSON.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AporteResponseDTO {
    // END-GOAL-06 (historial) y 12 (idAporte)
    private Long idAporte;
    private LocalDate fecha;
    private BigDecimal monto;
    private String descripcion;

    // END-GOAL-12 y 13: como queda la meta despues del cambio
    private BigDecimal montoAportado;
    private BigDecimal porcentaje;
    private EstadoMeta estadoMeta;      // solo END-GOAL-12
}