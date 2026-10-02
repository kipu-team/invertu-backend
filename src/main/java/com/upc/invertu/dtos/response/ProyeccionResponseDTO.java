package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** END-GOAL-07 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProyeccionResponseDTO {
    private LocalDate fechaEstimada;
    private String mensaje;
}
