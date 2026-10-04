package com.upc.invertu.serviciosimpl;

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
import com.upc.invertu.servicios.InicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
        // Mismo criterio y mensaje que el calendario (END-CAL-01)
        if (mes < 1 || mes > 12 || anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new ReglaNegocioException(CalendarioServiceImpl.MENSAJE_MES_INVALIDO);
        }
        YearMonth mesConsultado = YearMonth.of(anio, mes);
        if (mesConsultado.isAfter(YearMonth.now())) {
            throw new ReglaNegocioException(MENSAJE_MES_FUTURO);
        }

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

    // Siempre 2 decimales (0 -> 0.00); null se trata como 0 para no devolver nunca null
    private BigDecimal dosDecimales(BigDecimal monto) {
        return (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
    }
}
