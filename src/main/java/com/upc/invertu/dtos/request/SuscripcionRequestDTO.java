package com.upc.invertu.dtos.request;

import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-SUB-01, END-SUB-05 */
@Getter
@Setter
@NoArgsConstructor
public class SuscripcionRequestDTO {
    @NotBlank(message = "Ingresa el nombre del servicio")
    @Size(max = 100, message = "El nombre del servicio puede tener como máximo 100 caracteres")
    private String nombreServicio;

    @NotNull(message = "Ingresa el monto")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a S/ 0.00")
    private BigDecimal monto;

    @NotNull(message = "Selecciona una frecuencia")
    private FrecuenciaSuscripcion frecuencia;

    // No puede ser anterior a hoy: @FutureOrPresent acepta el dia de hoy
    @NotNull(message = "Ingresa la próxima fecha de cobro")
    @FutureOrPresent(message = "La próxima fecha de cobro no puede ser anterior a la fecha actual")
    private LocalDate proximaFechaCobro;

    // Opcional
    @Size(max = 500, message = "La descripción puede tener como máximo 500 caracteres")
    private String descripcion;
}
