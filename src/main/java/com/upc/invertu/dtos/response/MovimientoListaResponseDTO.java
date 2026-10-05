package com.upc.invertu.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** END-TRX-01 */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoListaResponseDTO {
    private int total;
    private List<MovimientoItemResponseDTO> movimientos;
}
