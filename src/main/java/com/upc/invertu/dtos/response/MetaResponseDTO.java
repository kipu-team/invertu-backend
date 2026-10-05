package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.EstadoMeta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-01, 08, 09, 10, 14 */
@Getter
@Setter
@NoArgsConstructor
public class MetaResponseDTO {
    private Long idMeta;
    private String nombre;
    private BigDecimal montoObjetivo;
    private BigDecimal montoAportado;   // suma de sus aportes
    private LocalDate fechaObjetivo;
    private EstadoMeta estado;
}
