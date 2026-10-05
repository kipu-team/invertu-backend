package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** END-DASH-03: un punto de evolucionSemanal (semana 1 = dias 1 a 7, semana 2 = dias 8 a 14, ...) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SemanaGraficoResponseDTO {
    private int semana;
    private BigDecimal ingresos;
    private BigDecimal gastos;
}
