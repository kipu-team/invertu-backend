package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** END-DASH-02: indicadores del mes (montos con 2 decimales, nunca null) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresResponseDTO {
    private BigDecimal ingresos;          // ingresosFijos + ingresosVariables
    private BigDecimal ingresosFijos;
    private BigDecimal ingresosVariables;
    private BigDecimal gastos;            // gastosFijos + gastosVariables
    private BigDecimal gastosFijos;
    private BigDecimal gastosVariables;
    private BigDecimal aportesMetas;      // aportes registrados en el mes
    private BigDecimal disponible;        // ingresos - gastos - aportesMetas (puede ser negativo)
    private BigDecimal ahorroTotal;       // aportes de metas ACTIVA o CUMPLIDA, de todos los meses
}
