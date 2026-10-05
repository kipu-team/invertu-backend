package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-DASH-01 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrientacionResponseDTO {
    private String nombreEstudiante;     // nombres del estudiante para el saludo "¡HOLA, [nombre]!"
    private boolean movimientoRegistrado; // tiene al menos un ingreso o gasto
    private boolean metaRegistrada;       // tiene al menos una meta, en cualquier estado
    private boolean mostrarOrientacion;   // true si le falta el movimiento o la meta
}
