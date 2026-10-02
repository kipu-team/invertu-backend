package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.AporteRequestDTO;
import com.upc.invertu.dtos.response.AporteResponseDTO;

import java.util.List;

/** EP-05: aportes a una meta */
public interface AporteService {
    List<AporteResponseDTO> listar(Long idMeta);             // END-GOAL-06
    AporteResponseDTO registrar(Long idMeta, AporteRequestDTO dto);        // END-GOAL-12
    AporteResponseDTO eliminar(Long idMeta, Long idAporte);                // END-GOAL-13
}
