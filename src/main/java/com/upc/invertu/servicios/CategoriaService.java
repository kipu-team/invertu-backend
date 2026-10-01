package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.CategoriaRequestDTO;
import com.upc.invertu.dtos.response.CategoriaResponseDTO;
import com.upc.invertu.dtos.response.MensajeResponseDTO;

import java.util.List;

/** EP-03: categorias predeterminadas y personalizadas */
public interface CategoriaService {
    // TODO: declarar las operaciones del modulo
    List<CategoriaResponseDTO> listarDisponibles();          // END-CAT-01
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);     // END-CAT-02
    MensajeResponseDTO desactivar(Long idCategoria);         // END-CAT-03
}
