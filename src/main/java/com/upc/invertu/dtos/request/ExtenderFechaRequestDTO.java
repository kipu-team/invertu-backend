package com.upc.invertu.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** END-GOAL-09: peticion para extender la fecha objetivo de una meta vencida */
@Getter
@Setter
@NoArgsConstructor
public class ExtenderFechaRequestDTO {
    // El id de la meta va por @PathVariable, no aqui.
    // Sin @Future: la validacion de "posterior a hoy" se hace en el Service,
    // despues de comprobar que la meta este vencida (END-GOAL-09)
    private LocalDate nuevaFechaObjetivo;
}
