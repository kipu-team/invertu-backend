package com.upc.invertu.servicios;

import com.upc.invertu.dtos.response.GraficosResponseDTO;
import com.upc.invertu.dtos.response.IndicadoresResponseDTO;
import com.upc.invertu.dtos.response.OrientacionResponseDTO;

/** EP-02: orientacion, indicadores y graficos del mes */
public interface InicioService {

    /** END-DASH-01 (US-05): datos para mostrar u ocultar la orientacion inicial */
    OrientacionResponseDTO obtenerOrientacion();

    /** END-DASH-02 (US-06 y US-07): indicadores del mes (actual o anterior) */
    IndicadoresResponseDTO obtenerIndicadores(int anio, int mes);

    /** END-DASH-03 (US-06 y US-08): evolucion semanal y totales por categoria del mes */
    GraficosResponseDTO obtenerGraficos(int anio, int mes);
}
