package com.upc.invertu.dtos.request;

import com.upc.invertu.entidades.enums.FrecuenciaAporte;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-01, END-GOAL-08 */
@Getter
@Setter
@NoArgsConstructor
public class MetaRequestDTO {
    @NotBlank(message = "Ingresa un nombre")
    @Size(max = 100, message = "El nombre puede tener como máximo 100 caracteres")
    private String nombre;

    @NotNull(message = "Ingresa un monto objetivo")
    @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor a S/ 0.00")
    @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo dos decimales")
    private BigDecimal montoObjetivo;

    @NotNull(message = "Ingresa una fecha objetivo")
    @Future(message = "La fecha objetivo debe ser posterior a hoy")
    private LocalDate fechaObjetivo;

    // Opcional: si no se envia, el Service usa SEMANAL
    private FrecuenciaAporte frecuenciaAporte;

    // Opcional
    @Size(max = 500, message = "La descripción puede tener como máximo 500 caracteres")
    private String descripcion;
}
