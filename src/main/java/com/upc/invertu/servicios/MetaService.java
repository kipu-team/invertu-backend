package com.upc.invertu.servicios;

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
}
