package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.ExtenderFechaRequestDTO;
import com.upc.invertu.dtos.request.MetaRequestDTO;
import com.upc.invertu.dtos.response.*;

import java.util.List;

/** EP-05: metas de ahorro */
public interface MetaService {
    MetaResponseDTO crear(MetaRequestDTO dto);              // END-GOAL-01
    EstadoLimiteResponseDTO consultarEstadoLimite();        // END-GOAL-02
    List<MetaResumenResponseDTO> listarActivas();           // END-GOAL-03
    List<MetaResumenResponseDTO> listarFinalizadas();       // END-GOAL-04
    MetaDetalleResponseDTO obtenerDetalle(Long idMeta);      // END-GOAL-05
    ProyeccionResponseDTO obtenerProyeccion(Long idMeta);    // END-GOAL-07
    MetaResponseDTO actualizar(Long idMeta, MetaRequestDTO dto); // END-GOAL-08
    MetaResponseDTO extenderFecha(Long idMeta, ExtenderFechaRequestDTO dto); // END-GOAL-09
    MetaResponseDTO cancelar(Long idMeta);          // END-GOAL-10
    MensajeResponseDTO eliminar(Long idMeta);       // END-GOAL-11
    MetaResponseDTO reactivar(Long idMeta);         // END-GOAL-14
}
