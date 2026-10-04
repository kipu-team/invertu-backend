package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.serviciosimpl.InicioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** US-05: orientacion inicial (END-DASH-01) */
@ExtendWith(MockitoExtension.class)
class InicioServiceImplTest {

    @Mock
    private MovimientoRepositorio movimientoRepositorio;

    @Mock
    private MetaRepositorio metaRepositorio;

    @Mock
    private EstudianteAutenticado estudianteAutenticado;

    @InjectMocks
    private InicioServiceImpl inicioService;

    @BeforeEach
    void setUp() {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        estudiante.setNombres("Ana");
        when(estudianteAutenticado.obtener()).thenReturn(estudiante);
    }

    private void prepararDatos(boolean tieneMovimiento, boolean tieneMeta) {
        when(movimientoRepositorio.existsByEstudianteIdEstudiante(1L)).thenReturn(tieneMovimiento);
        when(metaRepositorio.existsByEstudianteIdEstudiante(1L)).thenReturn(tieneMeta);
    }

    @Test
    void sinDatos_muestraOrientacion() {
        prepararDatos(false, false);

        OrientacionResponseDTO respuesta = inicioService.obtenerOrientacion();

        assertEquals("Ana", respuesta.getNombreEstudiante());
        assertFalse(respuesta.isMovimientoRegistrado());
        assertFalse(respuesta.isMetaRegistrada());
        assertTrue(respuesta.isMostrarOrientacion());
    }

    @Test
    void soloMovimiento_muestraOrientacion() {
        prepararDatos(true, false);

        OrientacionResponseDTO respuesta = inicioService.obtenerOrientacion();

        assertTrue(respuesta.isMovimientoRegistrado());
        assertFalse(respuesta.isMetaRegistrada());
        assertTrue(respuesta.isMostrarOrientacion());
    }

    @Test
    void soloMeta_muestraOrientacion() {
        prepararDatos(false, true);

        OrientacionResponseDTO respuesta = inicioService.obtenerOrientacion();

        assertFalse(respuesta.isMovimientoRegistrado());
        assertTrue(respuesta.isMetaRegistrada());
        assertTrue(respuesta.isMostrarOrientacion());
    }

    @Test
    void movimientoYMeta_ocultaOrientacion() {
        prepararDatos(true, true);

        OrientacionResponseDTO respuesta = inicioService.obtenerOrientacion();

        assertTrue(respuesta.isMovimientoRegistrado());
        assertTrue(respuesta.isMetaRegistrada());
        assertFalse(respuesta.isMostrarOrientacion());
    }
}
