package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** END-SUB-07 */
@Getter
@Setter
@NoArgsConstructor
public class ReactivarSuscripcionRequestDTO {
    @NotNull(message = "Ingresa la próxima fecha de cobro")
    private LocalDate proximaFechaCobro;
}