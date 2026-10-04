package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.AlertaSaldoRequestDTO;
import com.upc.invertu.dtos.request.RecordatorioRequestDTO;
import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.AlertaSaldoResponseDTO;
import com.upc.invertu.dtos.response.RecordatorioResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;

import java.time.LocalDate;
import java.util.List;

/** EP-06 suscripciones, recordatorio y alerta de saldo */
public interface SuscripcionService {
    SuscripcionResponseDTO registrar(SuscripcionRequestDTO dto);                              // END-SUB-01
    List<String> listarFrecuencias();                                                         // END-SUB-02
    List<SuscripcionResumenResponseDTO> listar(String estado);                                // END-SUB-03
    SuscripcionDetalleResponseDTO obtenerDetalle(Long idSuscripcion);                         // END-SUB-04
    SuscripcionResponseDTO editarSuscripcion(Long id, SuscripcionRequestDTO request);         // END-SUB-05
    SuscripcionResponseDTO cancelarSuscripcion(Long id);                                      // END-SUB-06
    SuscripcionResponseDTO reactivarSuscripcion(Long id, LocalDate proximaFechaCobro);        // END-SUB-07
    RecordatorioResponseDTO configurarRecordatorio(Long id, RecordatorioRequestDTO request);  // END-SUB-08
    AlertaSaldoResponseDTO configurarAlertaSaldo(Long id, AlertaSaldoRequestDTO request);     // END-SUB-09
}