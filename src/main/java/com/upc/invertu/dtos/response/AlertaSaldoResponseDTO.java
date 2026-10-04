package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-SUB-09 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSaldoResponseDTO {
    private Long idSuscripcion;
    private Boolean alertaSaldoActiva;
}