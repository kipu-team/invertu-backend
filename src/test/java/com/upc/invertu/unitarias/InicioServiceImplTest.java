package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.response.IndicadoresResponseDTO;
import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.AporteRepositorio;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** US-05: orientacion inicial (END-DASH-01). US-06 y US-07: indicadores del mes (END-DASH-02) */
@ExtendWith(MockitoExtension.class)
class InicioServiceImplTest {

    @Mock
    private MovimientoRepositorio movimientoRepositorio;

    @Mock
    private MetaRepositorio metaRepositorio;

    @Mock
    private AporteRepositorio aporteRepositorio;

    @Mock
    private EstudianteAutenticado estudianteAutenticado;

    @InjectMocks
    private InicioServiceImpl inicioService;

    @BeforeEach
    void setUp() {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        estudiante.setNombres("Ana");
        // lenient: el test de mes futuro falla antes de pedir el estudiante
        lenient().when(estudianteAutenticado.obtener()).thenReturn(estudiante);
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

    // ---------- END-DASH-02: indicadores del mes ----------

    // Mes pasado fijo: siempre es valido sin importar la fecha de hoy
    private static final int ANIO = 2025;
    private static final int MES = 3;
    private static final LocalDate INICIO = LocalDate.of(2025, 3, 1);
    private static final LocalDate FIN = LocalDate.of(2025, 3, 31);

    private void prepararIndicadores(List<Object[]> totales, String aportesMes, String ahorroTotal) {
        when(movimientoRepositorio.totalesDelMes(1L, INICIO, FIN)).thenReturn(totales);
        when(aporteRepositorio.aportesDelMes(1L, INICIO, FIN)).thenReturn(new BigDecimal(aportesMes));
        when(aporteRepositorio.ahorroTotal(1L, List.of(EstadoMeta.ACTIVA, EstadoMeta.CUMPLIDA)))
                .thenReturn(new BigDecimal(ahorroTotal));
    }

    private Object[] fila(TipoMovimiento tipo, Clasificacion clasificacion, String suma) {
        return new Object[]{tipo, clasificacion, new BigDecimal(suma)};
    }

    @Test
    void indicadores_conDatosCompletos_calculaTotales() {
        prepararIndicadores(List.of(
                fila(TipoMovimiento.INGRESO, Clasificacion.FIJO, "1500"),
                fila(TipoMovimiento.INGRESO, Clasificacion.VARIABLE, "300.50"),
                fila(TipoMovimiento.GASTO, Clasificacion.FIJO, "800"),
                fila(TipoMovimiento.GASTO, Clasificacion.VARIABLE, "250.25")), "200", "1200");

        IndicadoresResponseDTO respuesta = inicioService.obtenerIndicadores(ANIO, MES);

        assertEquals(new BigDecimal("1800.50"), respuesta.getIngresos());
        assertEquals(new BigDecimal("1500.00"), respuesta.getIngresosFijos());
        assertEquals(new BigDecimal("300.50"), respuesta.getIngresosVariables());
        assertEquals(new BigDecimal("1050.25"), respuesta.getGastos());
        assertEquals(new BigDecimal("800.00"), respuesta.getGastosFijos());
        assertEquals(new BigDecimal("250.25"), respuesta.getGastosVariables());
        assertEquals(new BigDecimal("200.00"), respuesta.getAportesMetas());
        assertEquals(new BigDecimal("550.25"), respuesta.getDisponible());
        assertEquals(new BigDecimal("1200.00"), respuesta.getAhorroTotal());
    }

    @Test
    void indicadores_sinDatos_todoEnCero() {
        prepararIndicadores(List.of(), "0", "0");

        IndicadoresResponseDTO respuesta = inicioService.obtenerIndicadores(ANIO, MES);

        BigDecimal cero = new BigDecimal("0.00");
        assertEquals(cero, respuesta.getIngresos());
        assertEquals(cero, respuesta.getIngresosFijos());
        assertEquals(cero, respuesta.getIngresosVariables());
        assertEquals(cero, respuesta.getGastos());
        assertEquals(cero, respuesta.getGastosFijos());
        assertEquals(cero, respuesta.getGastosVariables());
        assertEquals(cero, respuesta.getAportesMetas());
        assertEquals(cero, respuesta.getDisponible());
        assertEquals(cero, respuesta.getAhorroTotal());
    }

    @Test
    void indicadores_gastosMayoresQueIngresos_disponibleNegativo() {
        prepararIndicadores(List.of(
                fila(TipoMovimiento.INGRESO, Clasificacion.VARIABLE, "500"),
                fila(TipoMovimiento.GASTO, Clasificacion.FIJO, "600")), "100", "100");

        IndicadoresResponseDTO respuesta = inicioService.obtenerIndicadores(ANIO, MES);

        assertEquals(new BigDecimal("-200.00"), respuesta.getDisponible());
        assertEquals(new BigDecimal("0.00"), respuesta.getIngresosFijos());
        assertEquals(new BigDecimal("0.00"), respuesta.getGastosVariables());
    }

    @Test
    void indicadores_mesFuturo_lanza400() {
        YearMonth siguiente = YearMonth.now().plusMonths(1);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> inicioService.obtenerIndicadores(siguiente.getYear(), siguiente.getMonthValue()));

        assertEquals("Solo puedes consultar el mes actual o meses anteriores", ex.getMessage());
        verifyNoInteractions(movimientoRepositorio, aporteRepositorio, estudianteAutenticado);
    }
}
