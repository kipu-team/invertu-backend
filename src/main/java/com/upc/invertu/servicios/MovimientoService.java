package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.MovimientoResponseDTO;

/** EP-03: ingresos y gastos */
public interface MovimientoService {
    MovimientoResponseDTO registrar(MovimientoRequestDTO dto);
}
