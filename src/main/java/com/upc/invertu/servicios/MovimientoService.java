package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.MovimientoDetalleResponseDTO;
import com.upc.invertu.dtos.response.MovimientoListaResponseDTO;
import com.upc.invertu.dtos.response.MovimientoResponseDTO;

/** EP-03: ingresos y gastos */
public interface MovimientoService {
    MovimientoResponseDTO registrar(MovimientoRequestDTO dto);
    MovimientoListaResponseDTO listarDelMes(int anio, int mes);       // END-TRX-01
    MovimientoDetalleResponseDTO obtenerDetalle(Long idMovimiento);   // END-TRX-04
}
