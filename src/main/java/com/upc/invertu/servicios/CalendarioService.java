package com.upc.invertu.servicios;

import com.upc.invertu.dtos.response.EventoCalendarioResponseDTO;

import java.util.List;

/** EP-07: calendario financiero (solo Premium) */
public interface CalendarioService {
    List<EventoCalendarioResponseDTO> listarEventosDelMes(int anio, int mes); // END-CAL-01
}
