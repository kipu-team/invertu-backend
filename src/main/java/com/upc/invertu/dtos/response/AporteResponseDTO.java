package com.upc.invertu.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-06, 12, 13 */
@Getter
@Setter
@NoArgsConstructor
public class AporteResponseDTO {
    private Long idAporte;
    private LocalDate fecha;
    private BigDecimal monto;
    private String descripcion;
}
