package com.upc.invertu.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-GOAL-02 */
@Getter
@Setter
@NoArgsConstructor
public class EstadoLimiteResponseDTO {
    private String rol;                 // ROLE_FREE o ROLE_PREMIUM
    private int metasActivas;
    private Integer limite;             // null = sin limite (Premium)
    private boolean limiteAlcanzado;
}
