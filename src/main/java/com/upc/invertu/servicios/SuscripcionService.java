package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;

import java.time.LocalDate;
import java.util.List;

/** EP-06: suscripciones, recordatorio y alerta de saldo */
public interface SuscripcionService {
    SuscripcionResponseDTO registrar(SuscripcionRequestDTO dto);   // END-SUB-01
    List<String> listarFrecuencias();
    List<SuscripcionResumenResponseDTO> listar(String estado);
    SuscripcionDetalleResponseDTO obtenerDetalle(Long idSuscripcion);
    SuscripcionResponseDTO cancelarSuscripcion(Long id);
    SuscripcionResponseDTO reactivarSuscripcion(Long id, LocalDate proximaFechaCobro);

}
