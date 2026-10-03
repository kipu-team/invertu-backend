package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.EventoCalendarioResponseDTO;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.*;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import com.upc.invertu.entidades.Meta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.upc.invertu.dtos.response.EventosDiaResponseDTO;

import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CalendarioServiceImpl implements CalendarioService {
    public static final String MENSAJE_MES_INVALIDO = "Mes o año inválido";
    public static final String MENSAJE_FECHA_INVALIDA = "Fecha inválida";
    // Rango de anios aceptado: evita recorrer fechas sin sentido (por ejemplo, anio=0 o anio=99999)
    private static final int ANIO_MINIMO = 2000;
    private static final int ANIO_MAXIMO = 2100;

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private AporteRepositorio aporteRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-CAL-01: fechas limite de metas, aportes programados y cobros de suscripciones del mes */
    @Override
    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDTO> listarEventosDelMes(int anio, int mes) {
        if (mes < 1 || mes > 12 || anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new ReglaNegocioException(MENSAJE_MES_INVALIDO);
        }
        return generarEventos(YearMonth.of(anio, mes));
    }

    /** END-CAL-02: genera los eventos del mes de la fecha y se queda con los de ese dia */
    @Override
    @Transactional(readOnly = true)
    public EventosDiaResponseDTO listarEventosDelDia(String fecha) {
        LocalDate dia = convertirFecha(fecha);

        List<EventoCalendarioResponseDTO> eventos = generarEventos(YearMonth.from(dia)).stream()
                .filter(evento -> evento.getFecha().equals(dia))
                .toList();
        // La fecha ya va en la respuesta: no se repite en cada evento (se oculta con NON_NULL)
        eventos.forEach(evento -> evento.setFecha(null));

        return new EventosDiaResponseDTO(dia, eventos.size(), eventos);
    }
    /**
     * T-52: genera los eventos de un mes, ordenados por fecha.
     * - META_LIMITE: fecha objetivo de la meta; monto = lo que falta ahorrar.
     * - META_APORTE: fechas programadas desde proximaFechaAporte segun la frecuencia, hasta la fecha objetivo;
     *   monto = monto sugerido (restante / aportes pendientes).
     * - SUSCRIPCION: cobros desde proximaFechaCobro segun la frecuencia; monto = precio de la suscripcion.
     */
    private List<EventoCalendarioResponseDTO> generarEventos(YearMonth mes) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        LocalDate inicio = mes.atDay(1);
        LocalDate fin = mes.atEndOfMonth();

        List<EventoCalendarioResponseDTO> eventos = new ArrayList<>();

        Map<Long, BigDecimal> aportado = obtenerAportadoPorMeta(idEstudiante);
        for (Meta meta : metaRepositorio.findByEstudianteIdEstudianteAndEstado(idEstudiante, EstadoMeta.ACTIVA)) {
            BigDecimal restante = meta.getMontoObjetivo()
                    .subtract(aportado.getOrDefault(meta.getIdMeta(), BigDecimal.ZERO))
                    .max(BigDecimal.ZERO);
            agregarEventosMeta(eventos, meta, restante, inicio, fin);
        }

        for (Suscripcion suscripcion : suscripcionRepositorio
                .findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(idEstudiante, EstadoSuscripcion.ACTIVA)) {
            agregarEventosSuscripcion(eventos, suscripcion, inicio, fin);
        }

        // Por fecha; en el mismo dia: fecha limite, aporte y cobro (orden del enum), y luego por nombre
        eventos.sort(Comparator.comparing(EventoCalendarioResponseDTO::getFecha)
                .thenComparing(EventoCalendarioResponseDTO::getTipo)
                .thenComparing(EventoCalendarioResponseDTO::getNombre));
        return eventos;
    }

    private void agregarEventosMeta(List<EventoCalendarioResponseDTO> eventos, Meta meta, BigDecimal restante,
                                    LocalDate inicio, LocalDate fin) {
        LocalDate fechaObjetivo = meta.getFechaObjetivo();
        if (estaEnRango(fechaObjetivo, inicio, fin)) {
            eventos.add(new EventoCalendarioResponseDTO(fechaObjetivo, TipoEvento.META_LIMITE,
                    meta.getIdMeta(), meta.getNombre(), restante));
        }

        // SIN_FRECUENCIA no tiene aportes programados (proximaFechaAporte es null)
        if (meta.getFrecuenciaAporte() == FrecuenciaAporte.SIN_FRECUENCIA || meta.getProximaFechaAporte() == null
                || restante.signum() == 0) {
            return;
        }

        List<LocalDate> fechasAporte = fechasProgramadasAporte(meta);
        if (fechasAporte.isEmpty()) {
            return;
        }
        BigDecimal sugerido = restante.divide(BigDecimal.valueOf(fechasAporte.size()), 2, RoundingMode.HALF_UP);
        for (LocalDate fecha : fechasAporte) {
            if (estaEnRango(fecha, inicio, fin)) {
                eventos.add(new EventoCalendarioResponseDTO(fecha, TipoEvento.META_APORTE,
                        meta.getIdMeta(), meta.getNombre(), sugerido));
            }
        }
    }

    /** Aportes pendientes: desde proximaFechaAporte, cada periodo, sin pasar la fecha objetivo */
    private List<LocalDate> fechasProgramadasAporte(Meta meta) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate fechaObjetivo = meta.getFechaObjetivo();
        LocalDate primera = meta.getProximaFechaAporte();
        // Se avanza siempre desde la primera fecha (n periodos) para que un 31 no se corra a 28/30 en adelante
        for (int n = 0; ; n++) {
            LocalDate fecha = sumarPeriodos(primera, meta.getFrecuenciaAporte(), n);
            if (fecha.isAfter(fechaObjetivo)) {
                break;
            }
            fechas.add(fecha);
        }
        return fechas;
    }

    private void agregarEventosSuscripcion(List<EventoCalendarioResponseDTO> eventos, Suscripcion suscripcion,
                                           LocalDate inicio, LocalDate fin) {
        LocalDate primera = suscripcion.getProximaFechaCobro();
        for (int n = 0; ; n++) {
            LocalDate cobro = sumarPeriodos(primera, suscripcion.getFrecuencia(), n);
            if (cobro.isAfter(fin)) {
                break;
            }
            // Los cobros anteriores al mes consultado se saltan; solo se agregan los del mes
            if (!cobro.isBefore(inicio)) {
                eventos.add(new EventoCalendarioResponseDTO(cobro, TipoEvento.SUSCRIPCION,
                        suscripcion.getIdSuscripcion(), suscripcion.getNombreServicio(), suscripcion.getMonto()));
            }
        }
    }

    // La fecha llega como texto (YYYY-MM-DD) para responder 400 "Fecha inválida" con el mensaje de la US-34
    private LocalDate convertirFecha(String fecha) {
        try {
            LocalDate dia = LocalDate.parse(fecha.trim());
            if (dia.getYear() < ANIO_MINIMO || dia.getYear() > ANIO_MAXIMO) {
                throw new ReglaNegocioException(MENSAJE_FECHA_INVALIDA);
            }
            return dia;
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(MENSAJE_FECHA_INVALIDA);
        }
    }

    private boolean estaEnRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    // Convierte las filas [idMeta, suma] de aportadoPorMeta en un Map para buscar rapido por idMeta
    private Map<Long, BigDecimal> obtenerAportadoPorMeta(Long idEstudiante) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        for (Object[] fila : aporteRepositorio.aportadoPorMeta(idEstudiante)) {
            mapa.put((Long) fila[0], (BigDecimal) fila[1]);
        }
        return mapa;
    }

    private LocalDate sumarPeriodos(LocalDate desde, FrecuenciaAporte frecuencia, int periodos) {
        return switch (frecuencia) {
            case DIARIA -> desde.plusDays(periodos);
            case SEMANAL -> desde.plusWeeks(periodos);
            case MENSUAL -> desde.plusMonths(periodos);
            case SIN_FRECUENCIA -> desde;
        };
    }

    private LocalDate sumarPeriodos(LocalDate desde, FrecuenciaSuscripcion frecuencia, int periodos) {
        return switch (frecuencia) {
            case SEMANAL -> desde.plusWeeks(periodos);
            case MENSUAL -> desde.plusMonths(periodos);
            case TRIMESTRAL -> desde.plusMonths(3L * periodos);
            case SEMESTRAL -> desde.plusMonths(6L * periodos);
            case ANUAL -> desde.plusYears(periodos);
        };
    }
}
