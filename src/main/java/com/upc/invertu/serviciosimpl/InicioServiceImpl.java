package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.GraficosResponseDTO;
import com.upc.invertu.dtos.response.IndicadoresResponseDTO;
import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.dtos.response.SemanaGraficoResponseDTO;
import com.upc.invertu.dtos.response.TotalCategoriaResponseDTO;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.InicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class InicioServiceImpl implements InicioService {
    public static final String MENSAJE_MES_FUTURO = "Solo puedes consultar el mes actual o meses anteriores";
    // Mismo rango de anios que CalendarioServiceImpl
    private static final int ANIO_MINIMO = 2000;
    private static final int ANIO_MAXIMO = 2100;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private AporteRepositorio aporteRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-DASH-01: la orientacion se muestra hasta que registre un movimiento y cree una meta */
    @Override
    @Transactional(readOnly = true)
    public OrientacionResponseDTO obtenerOrientacion() {
        Estudiante estudiante = estudianteAutenticado.obtener();
        Long idEstudiante = estudiante.getIdEstudiante();

        boolean movimientoRegistrado = movimientoRepositorio.existsByEstudianteIdEstudiante(idEstudiante);
        boolean metaRegistrada = metaRepositorio.existsByEstudianteIdEstudiante(idEstudiante);
        boolean mostrarOrientacion = !(movimientoRegistrado && metaRegistrada);

        return new OrientacionResponseDTO(estudiante.getNombres(), movimientoRegistrado,
                metaRegistrada, mostrarOrientacion);
    }

    /** END-DASH-02 (US-06 y US-07): ingresos, gastos, aportes, disponible y ahorro total del mes */
    @Override
    @Transactional(readOnly = true)
    public IndicadoresResponseDTO obtenerIndicadores(int anio, int mes) {
        YearMonth mesConsultado = validarMesConsultable(anio, mes);

        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        LocalDate inicio = mesConsultado.atDay(1);
        LocalDate fin = mesConsultado.atEndOfMonth();

        // Sin movimientos la lista llega vacia: todos los totales se quedan en 0
        BigDecimal ingresosFijos = BigDecimal.ZERO;
        BigDecimal ingresosVariables = BigDecimal.ZERO;
        BigDecimal gastosFijos = BigDecimal.ZERO;
        BigDecimal gastosVariables = BigDecimal.ZERO;
        for (Object[] fila : movimientoRepositorio.totalesDelMes(idEstudiante, inicio, fin)) {
            TipoMovimiento tipo = (TipoMovimiento) fila[0];
            Clasificacion clasificacion = (Clasificacion) fila[1];
            BigDecimal suma = (BigDecimal) fila[2];
            if (tipo == TipoMovimiento.INGRESO) {
                if (clasificacion == Clasificacion.FIJO) {
                    ingresosFijos = suma;
                } else {
                    ingresosVariables = suma;
                }
            } else {
                if (clasificacion == Clasificacion.FIJO) {
                    gastosFijos = suma;
                } else {
                    gastosVariables = suma;
                }
            }
        }

        BigDecimal ingresos = ingresosFijos.add(ingresosVariables);
        BigDecimal gastos = gastosFijos.add(gastosVariables);
        BigDecimal aportesMetas = aporteRepositorio.aportesDelMes(idEstudiante, inicio, fin);
        BigDecimal disponible = ingresos.subtract(gastos).subtract(aportesMetas);
        BigDecimal ahorroTotal = aporteRepositorio.ahorroTotal(idEstudiante,
                List.of(EstadoMeta.ACTIVA, EstadoMeta.CUMPLIDA));

        return new IndicadoresResponseDTO(dosDecimales(ingresos), dosDecimales(ingresosFijos),
                dosDecimales(ingresosVariables), dosDecimales(gastos), dosDecimales(gastosFijos),
                dosDecimales(gastosVariables), dosDecimales(aportesMetas), dosDecimales(disponible),
                dosDecimales(ahorroTotal));
    }

    /** END-DASH-03 (US-06 y US-08): evolucion semanal, gastos por categoria e ingresos por fuente del mes */
    @Override
    @Transactional(readOnly = true)
    public GraficosResponseDTO obtenerGraficos(int anio, int mes) {
        YearMonth mesConsultado = validarMesConsultable(anio, mes);

        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        LocalDate inicio = mesConsultado.atDay(1);
        LocalDate fin = mesConsultado.atEndOfMonth();

        List<SemanaGraficoResponseDTO> evolucionSemanal = agruparPorSemana(
                movimientoRepositorio.evolucionSemanal(idEstudiante, inicio, fin), mesConsultado);
        List<TotalCategoriaResponseDTO> gastosPorCategoria = aTotalesPorCategoria(
                movimientoRepositorio.totalesPorCategoria(idEstudiante, TipoMovimiento.GASTO, inicio, fin));
        List<TotalCategoriaResponseDTO> ingresosPorFuente = aTotalesPorCategoria(
                movimientoRepositorio.totalesPorCategoria(idEstudiante, TipoMovimiento.INGRESO, inicio, fin));

        return new GraficosResponseDTO(evolucionSemanal, gastosPorCategoria, ingresosPorFuente);
    }

    // Validacion compartida por END-DASH-02 y END-DASH-03
    private YearMonth validarMesConsultable(int anio, int mes) {
        // Mismo criterio y mensaje que el calendario (END-CAL-01)
        if (mes < 1 || mes > 12 || anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new ReglaNegocioException(CalendarioServiceImpl.MENSAJE_MES_INVALIDO);
        }
        YearMonth mesConsultado = YearMonth.of(anio, mes);
        if (mesConsultado.isAfter(YearMonth.now())) {
            throw new ReglaNegocioException(MENSAJE_MES_FUTURO);
        }
        return mesConsultado;
    }

    // Filas [fecha, tipo, suma] por dia -> semanas del mes (1 = dias 1-7, 2 = dias 8-14, ..., 5 = dias 29-31).
    // Sin movimientos devuelve lista vacia; con movimientos devuelve todas las semanas (0 si no hubo datos)
    private List<SemanaGraficoResponseDTO> agruparPorSemana(List<Object[]> filasPorDia, YearMonth mesConsultado) {
        List<SemanaGraficoResponseDTO> semanas = new ArrayList<>();
        if (filasPorDia.isEmpty()) {
            return semanas;
        }

        int cantidadSemanas = (mesConsultado.lengthOfMonth() + 6) / 7;
        BigDecimal[] ingresos = new BigDecimal[cantidadSemanas];
        BigDecimal[] gastos = new BigDecimal[cantidadSemanas];
        Arrays.fill(ingresos, BigDecimal.ZERO);
        Arrays.fill(gastos, BigDecimal.ZERO);

        for (Object[] fila : filasPorDia) {
            LocalDate fecha = (LocalDate) fila[0];
            TipoMovimiento tipo = (TipoMovimiento) fila[1];
            BigDecimal suma = (BigDecimal) fila[2];
            int indice = (fecha.getDayOfMonth() - 1) / 7;
            if (tipo == TipoMovimiento.INGRESO) {
                ingresos[indice] = ingresos[indice].add(suma);
            } else {
                gastos[indice] = gastos[indice].add(suma);
            }
        }

        for (int i = 0; i < cantidadSemanas; i++) {
            semanas.add(new SemanaGraficoResponseDTO(i + 1, dosDecimales(ingresos[i]), dosDecimales(gastos[i])));
        }
        return semanas;
    }

    // Filas [nombreCategoria, suma], ya ordenadas de mayor a menor por la consulta
    private List<TotalCategoriaResponseDTO> aTotalesPorCategoria(List<Object[]> filas) {
        List<TotalCategoriaResponseDTO> totales = new ArrayList<>();
        for (Object[] fila : filas) {
            totales.add(new TotalCategoriaResponseDTO((String) fila[0], dosDecimales((BigDecimal) fila[1])));
        }
        return totales;
    }

    // Siempre 2 decimales (0 -> 0.00); null se trata como 0 para no devolver nunca null
    private BigDecimal dosDecimales(BigDecimal monto) {
        return (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
    }
}
