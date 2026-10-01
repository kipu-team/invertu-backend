package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.MetaRequestDTO;
import com.upc.invertu.dtos.response.EstadoLimiteResponseDTO;
import com.upc.invertu.dtos.response.MetaResponseDTO;

/** EP-05: metas de ahorro */
public interface MetaService {
    MetaResponseDTO crear(MetaRequestDTO dto);              // END-GOAL-01
    EstadoLimiteResponseDTO consultarEstadoLimite();        // END-GOAL-02
}
