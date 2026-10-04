package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** END-DASH-03: total de una categoria (usado en gastosPorCategoria e ingresosPorFuente) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalCategoriaResponseDTO {
    private String categoria;   // nombre de la categoria
    private BigDecimal total;
}
