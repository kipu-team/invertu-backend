package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-GOAL-12 */
@Getter
@Setter
@NoArgsConstructor
public class AporteRequestDTO {
    @NotNull(message = "Ingresa un monto")
    @DecimalMin(value = "0.01", message = "El aporte debe ser mayor a S/ 0.00")
    @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo dos decimales")
    private BigDecimal monto;

    @NotNull(message = "Ingresa una fecha")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    @Size(max = 250, message = "La descripción puede tener como máximo 250 caracteres")
    private String descripcion;
}
