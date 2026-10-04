package com.upc.invertu.servicios;

import com.upc.invertu.dtos.response.OrientacionResponseDTO;

/** EP-02: orientacion, indicadores y graficos del mes */
public interface InicioService {
    // TODO: declarar las demas operaciones del modulo (END-DASH-02 y 03)

    /** END-DASH-01 (US-05): datos para mostrar u ocultar la orientacion inicial */
    OrientacionResponseDTO obtenerOrientacion();
}
