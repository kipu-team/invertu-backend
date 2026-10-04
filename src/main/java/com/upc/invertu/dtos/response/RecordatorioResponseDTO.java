package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-SUB-08 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordatorioResponseDTO {
    private Long idSuscripcion;
    private Boolean recordatorioActivo;
    private Integer diasAnticipacion;   // null si el recordatorio esta desactivado
}