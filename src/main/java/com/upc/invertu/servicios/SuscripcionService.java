package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;

import java.util.List;

public interface SuscripcionService {
    SuscripcionResponseDTO crearSuscripcion(SuscripcionRequestDTO request);
    SuscripcionResponseDTO obtenerSuscripcionPorId(Long idSuscripcion);
    List<SuscripcionResponseDTO> obtenerSuscripcionesPorEstudiante(Long estudianteId);
    SuscripcionResponseDTO actualizarSuscripcion(Long idSuscripcion, SuscripcionRequestDTO request);
    void cancelarSuscripcion(Long idSuscripcion);
    void reactivarSuscripcion(Long idSuscripcion);
}