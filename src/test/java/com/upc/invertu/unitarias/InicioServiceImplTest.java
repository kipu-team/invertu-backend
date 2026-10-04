package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.response.GraficosResponseDTO;
import com.upc.invertu.dtos.response.IndicadoresResponseDTO;
import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.dtos.response.SemanaGraficoResponseDTO;
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

/**
 * US-05: orientacion inicial (END-DASH-01). US-06 y US-07: indicadores del mes (END-DASH-02).
 * US-06 y US-08: graficos del mes (END-DASH-03)
 */
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

    // ---------- END-DASH-03: graficos del mes (marzo 2025: 31 dias = 5 semanas) ----------

    private void prepararGraficos(List<Object[]> porDia, List<Object[]> gastos, List<Object[]> ingresos) {
        when(movimientoRepositorio.evolucionSemanal(1L, INICIO, FIN)).thenReturn(porDia);
        when(movimientoRepositorio.totalesPorCategoria(1L, TipoMovimiento.GASTO, INICIO, FIN)).thenReturn(gastos);
        when(movimientoRepositorio.totalesPorCategoria(1L, TipoMovimiento.INGRESO, INICIO, FIN)).thenReturn(ingresos);
    }

    private Object[] dia(int dia, TipoMovimiento tipo, String suma) {
        return new Object[]{LocalDate.of(2025, 3, dia), tipo, new BigDecimal(suma)};
    }

    private Object[] categoria(String nombre, String total) {
        return new Object[]{nombre, new BigDecimal(total)};
    }

    private void assertSemana(SemanaGraficoResponseDTO semana, int numero, String ingresos, String gastos) {
        assertEquals(numero, semana.getSemana());
        assertEquals(new BigDecimal(ingresos), semana.getIngresos());
        assertEquals(new BigDecimal(gastos), semana.getGastos());
    }

    @Test
    void graficos_conDatos_agrupaPorSemanaYCategoria() {
        prepararGraficos(
                List.of(dia(1, TipoMovimiento.INGRESO, "1500"),
                        dia(7, TipoMovimiento.GASTO, "100"),
                        dia(8, TipoMovimiento.GASTO, "50.50"),
                        dia(14, TipoMovimiento.GASTO, "20"),
                        dia(31, TipoMovimiento.INGRESO, "300")),
                List.of(categoria("Comida", "120"), categoria("Transporte", "50.50")),
                List.of(categoria("Mesada", "1500"), categoria("Freelance", "300")));

        GraficosResponseDTO respuesta = inicioService.obtenerGraficos(ANIO, MES);

        List<SemanaGraficoResponseDTO> semanas = respuesta.getEvolucionSemanal();
        assertEquals(5, semanas.size());
        assertSemana(semanas.get(0), 1, "1500.00", "100.00");   // dias 1 y 7
        assertSemana(semanas.get(1), 2, "0.00", "70.50");       // dias 8 y 14
        assertSemana(semanas.get(2), 3, "0.00", "0.00");
        assertSemana(semanas.get(3), 4, "0.00", "0.00");
        assertSemana(semanas.get(4), 5, "300.00", "0.00");      // dia 31

        assertEquals(2, respuesta.getGastosPorCategoria().size());
        assertEquals("Comida", respuesta.getGastosPorCategoria().get(0).getCategoria());
        assertEquals(new BigDecimal("120.00"), respuesta.getGastosPorCategoria().get(0).getTotal());
        assertEquals("Mesada", respuesta.getIngresosPorFuente().get(0).getCategoria());
        assertEquals(new BigDecimal("300.00"), respuesta.getIngresosPorFuente().get(1).getTotal());
    }

    @Test
    void graficos_soloGastos_ingresosPorFuenteVacio() {
        prepararGraficos(
                List.<Object[]>of(dia(10, TipoMovimiento.GASTO, "80")),
                List.<Object[]>of(categoria("Comida", "80")),
                List.of());

        GraficosResponseDTO respuesta = inicioService.obtenerGraficos(ANIO, MES);

        assertSemana(respuesta.getEvolucionSemanal().get(1), 2, "0.00", "80.00");
        assertEquals(1, respuesta.getGastosPorCategoria().size());
        assertNotNull(respuesta.getIngresosPorFuente());
        assertTrue(respuesta.getIngresosPorFuente().isEmpty());
    }

    @Test
    void graficos_sinDatos_tresListasVacias() {
        prepararGraficos(List.of(), List.of(), List.of());

        GraficosResponseDTO respuesta = inicioService.obtenerGraficos(ANIO, MES);

        assertTrue(respuesta.getEvolucionSemanal().isEmpty());
        assertTrue(respuesta.getGastosPorCategoria().isEmpty());
        assertTrue(respuesta.getIngresosPorFuente().isEmpty());
    }

    @Test
    void graficos_mesFuturo_lanza400() {
        YearMonth siguiente = YearMonth.now().plusMonths(1);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> inicioService.obtenerGraficos(siguiente.getYear(), siguiente.getMonthValue()));

        assertEquals("Solo puedes consultar el mes actual o meses anteriores", ex.getMessage());
        verifyNoInteractions(movimientoRepositorio, estudianteAutenticado);
    }
}
