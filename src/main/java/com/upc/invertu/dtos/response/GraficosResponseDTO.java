package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** END-DASH-03: datos de los graficos del mes (listas vacias cuando no hay datos, nunca null) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraficosResponseDTO {
    private List<SemanaGraficoResponseDTO> evolucionSemanal;
    private List<TotalCategoriaResponseDTO> gastosPorCategoria;   // de mayor a menor
    private List<TotalCategoriaResponseDTO> ingresosPorFuente;    // de mayor a menor
}
