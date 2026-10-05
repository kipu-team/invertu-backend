package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.AlertaSaldoRequestDTO;
import com.upc.invertu.dtos.request.RecordatorioRequestDTO;
import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.AlertaSaldoResponseDTO;
import com.upc.invertu.dtos.response.RecordatorioResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Plan;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.LimitePlanException;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    private static final List<Integer> DIAS_ANTICIPACION_VALIDOS = List.of(1, 3, 7);

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private PlanRepositorio planRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-SUB-01, registra una suscripcion del estudiante autenticado en estado activa */
    @Override
    @Transactional
    public SuscripcionResponseDTO registrar(SuscripcionRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEstudiante(estudiante);
        suscripcion.setNombreServicio(dto.getNombreServicio().trim());
        suscripcion.setDescripcion(limpiarTexto(dto.getDescripcion()));
        suscripcion.setMonto(dto.getMonto());
        suscripcion.setFrecuencia(dto.getFrecuencia());
        suscripcion.setProximaFechaCobro(dto.getProximaFechaCobro());
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setRecordatorioActivo(false);
        suscripcion.setAlertaSaldoActiva(false);


        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    /** END-SUB-02: frecuencias disponibles desde el enum, sin consultar la base de datos */
    @Override
    @Transactional(readOnly = true)
    public List<String> listarFrecuencias() {
        return List.of(FrecuenciaSuscripcion.values()).stream()
                .map(Enum::name)
                .toList();
    }

    /** END-SUB-03 (US-27): suscripciones del estudiante, opcionalmente filtradas por estado */
    @Override
    @Transactional(readOnly = true)
    public List<SuscripcionResumenResponseDTO> listar(String estado) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        List<Suscripcion> suscripciones;
        if (estado == null || estado.isBlank()) {
            suscripciones = suscripcionRepositorio
                    .findByEstudianteIdEstudianteOrderByProximaFechaCobroAsc(idEstudiante);
        } else {
            String estadoNorm = estado.trim().toUpperCase();
            try {
                EstadoSuscripcion enumEstado = EstadoSuscripcion.valueOf(estadoNorm);
                suscripciones = suscripcionRepositorio
                        .findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(idEstudiante, enumEstado);
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("Estado inválido. Valores permitidos: ACTIVA, CANCELADA");
            }
        }

        return suscripciones.stream()
                .map(s -> new SuscripcionResumenResponseDTO(
                        s.getIdSuscripcion(),
                        s.getNombreServicio(),
                        s.getMonto(),
                        String.valueOf(s.getFrecuencia()),
                        s.getProximaFechaCobro(),
                        String.valueOf(s.getEstado()),
                        s.getRecordatorioActivo(),
                        calcularPagoSinRegistrar(s)))
                .toList();
    }

    /** END-SUB-04 (US-27 y US-28): detalle con el historial de pagos y la categoria del ultimo pago */
    @Override
    @Transactional(readOnly = true)
    public SuscripcionDetalleResponseDTO obtenerDetalle(Long idSuscripcion) {
        Suscripcion s = buscarPropia(idSuscripcion);

        List<Movimiento> movimientos = movimientoRepositorio
                .findBySuscripcionIdSuscripcionOrderByFechaDesc(s.getIdSuscripcion());

        List<SuscripcionDetalleResponseDTO.PagoDTO> pagos = movimientos.stream()
                .map(m -> new SuscripcionDetalleResponseDTO.PagoDTO(
                        m.getFecha(),
                        m.getMonto(),
                        m.getMedioPago() != null ? m.getMedioPago().name() : null)) // el medio de pago es opcional
                .toList();

        // US-28: Registrar pago se precarga con la categoria del pago mas reciente
        SuscripcionDetalleResponseDTO.CategoriaPagoDTO ultimaCategoria = movimientos.isEmpty()
                ? null
                : new SuscripcionDetalleResponseDTO.CategoriaPagoDTO(
                movimientos.get(0).getCategoria().getIdCategoria(),
                movimientos.get(0).getCategoria().getNombre());

        return new SuscripcionDetalleResponseDTO(
                s.getIdSuscripcion(),
                s.getNombreServicio(),
                s.getDescripcion(),
                s.getMonto(),
                String.valueOf(s.getFrecuencia()),
                s.getProximaFechaCobro(),
                String.valueOf(s.getEstado()),
                s.getRecordatorioActivo(),
                s.getDiasAnticipacion(),
                s.getAlertaSaldoActiva(),
                calcularPagoSinRegistrar(s),
                ultimaCategoria,
                pagos);
    }

    /** END-SUB-05 (US-29): actualiza una suscripcion activa. Los pagos registrados no cambian */
    @Override
    @Transactional
    public SuscripcionResponseDTO editarSuscripcion(Long id, SuscripcionRequestDTO request) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden editar suscripciones activas");
        }

        suscripcion.setNombreServicio(request.getNombreServicio().trim());
        suscripcion.setDescripcion(limpiarTexto(request.getDescripcion()));
        suscripcion.setMonto(request.getMonto());
        suscripcion.setFrecuencia(request.getFrecuencia());
        suscripcion.setProximaFechaCobro(request.getProximaFechaCobro());

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    /** END-SUB-06 (US-30): cancela una suscripcion activa y desactiva su recordatorio y su alerta */
    @Override
    @Transactional
    public SuscripcionResponseDTO cancelarSuscripcion(Long id) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo puedes cancelar suscripciones activas");
        }

        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDate.now());
        suscripcion.setRecordatorioActivo(false);
        suscripcion.setAlertaSaldoActiva(false);
        suscripcion.setDiasAnticipacion(null);

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    /** END-SUB-07 (US-30): reactiva una suscripcion cancelada con una nueva proxima fecha de cobro */
    @Override
    @Transactional
    public SuscripcionResponseDTO reactivarSuscripcion(Long id, LocalDate proximaFechaCobro) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.CANCELADA) {
            throw new ReglaNegocioException("Solo puedes reactivar suscripciones canceladas");
        }

        if (proximaFechaCobro.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de cobro no puede ser anterior a hoy");
        }

        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaCancelacion(null);
        suscripcion.setProximaFechaCobro(proximaFechaCobro);

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    /**END-SUB-08 (US-31): activa o desactiva el recordatorio por correo.El limite de recordatorios activos se lee del plan del estudiante (Free: 3; Premium: sin limite).*/
    @Override
    @Transactional
    public RecordatorioResponseDTO configurarRecordatorio(Long id, RecordatorioRequestDTO request) {
        Estudiante estudiante = estudianteAutenticado.obtener();
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden configurar suscripciones activas");
        }

        if (Boolean.TRUE.equals(request.getActivo())) {
            if (!DIAS_ANTICIPACION_VALIDOS.contains(request.getDiasAnticipacion())) {
                throw new ReglaNegocioException("Días de anticipación no válidos");
            }

            // Solo se valida el limite si se activa un recordatorio NUEVO (cambiar los dias no cuenta)
            if (!Boolean.TRUE.equals(suscripcion.getRecordatorioActivo())) {
                validarLimiteRecordatorios(estudiante);
            }

            suscripcion.setRecordatorioActivo(true);
            suscripcion.setDiasAnticipacion(request.getDiasAnticipacion());
        } else {
            suscripcion.setRecordatorioActivo(false);
            suscripcion.setDiasAnticipacion(null);
        }

        Suscripcion guardada = suscripcionRepositorio.save(suscripcion);
        return new RecordatorioResponseDTO(
                guardada.getIdSuscripcion(), guardada.getRecordatorioActivo(), guardada.getDiasAnticipacion());
    }

    /** END-SUB-09 (US-32): activa o desactiva la alerta de saldo (solo Premium, validado en el controller) */
    @Override
    @Transactional
    public AlertaSaldoResponseDTO configurarAlertaSaldo(Long id, AlertaSaldoRequestDTO request) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden configurar suscripciones activas");
        }

        suscripcion.setAlertaSaldoActiva(request.getActiva());
        if (Boolean.FALSE.equals(request.getActiva())) {
            suscripcion.setUltimaFechaAlertaSaldo(null);
        }

        Suscripcion guardada = suscripcionRepositorio.save(suscripcion);
        return new AlertaSaldoResponseDTO(guardada.getIdSuscripcion(), guardada.getAlertaSaldoActiva());
    }

    // Metodos auxiliares

    private Suscripcion buscarPropia(Long idSuscripcion) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        return suscripcionRepositorio.findByIdSuscripcionAndEstudianteIdEstudiante(idSuscripcion, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("La suscripción no existe"));
    }

    /** END-SUB-08: si el plan tiene limite (Free) y ya se alcanzo, 403. Premium no tiene limite (null). */
    private void validarLimiteRecordatorios(Estudiante estudiante) {
        String rol = estudiante.getRol().getNombre();
        Plan plan = planRepositorio.findByRolNombre(rol)
                .orElseThrow(() -> new IllegalStateException("No existe un plan para el rol " + rol));
        Integer limite = plan.getMaxRecordatorios();

        long activos = suscripcionRepositorio.countByEstudianteIdEstudianteAndEstadoAndRecordatorioActivoTrue(
                estudiante.getIdEstudiante(), EstadoSuscripcion.ACTIVA);
        if (limite != null && activos >= limite) {
            throw new LimitePlanException(
                    "Con el plan Free puedes activar recordatorios en hasta " + limite + " suscripciones");
        }
    }

    /**END-SUB-03 y 04*/
    private boolean calcularPagoSinRegistrar(Suscripcion s) {
        if (s.getEstado() != EstadoSuscripcion.ACTIVA || s.getProximaFechaCobro() == null) return false;

        LocalDate ultimoCobro = restarPeriodo(s.getProximaFechaCobro(), s.getFrecuencia());
        if (!ultimoCobro.isBefore(LocalDate.now())) return false;

        // Un cobro anterior a la creacion de la suscripcion no se le puede exigir al estudiante
        if (s.getFechaCreacion() != null && ultimoCobro.isBefore(s.getFechaCreacion().toLocalDate())) return false;

        return !movimientoRepositorio.existsBySuscripcionIdSuscripcionAndTipoAndFechaGreaterThanEqual(
                s.getIdSuscripcion(), TipoMovimiento.GASTO, ultimoCobro);
    }

    private LocalDate restarPeriodo(LocalDate fecha, FrecuenciaSuscripcion frecuencia) {
        return switch (frecuencia) {
            case SEMANAL    -> fecha.minusWeeks(1);
            case MENSUAL    -> fecha.minusMonths(1);
            case TRIMESTRAL -> fecha.minusMonths(3);
            case SEMESTRAL  -> fecha.minusMonths(6);
            case ANUAL      -> fecha.minusYears(1);
        };
    }

    // Texto opcional: si viene vacio o con solo espacios se guarda null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private SuscripcionResponseDTO aDTO(Suscripcion suscripcion) {
        SuscripcionResponseDTO dto = new SuscripcionResponseDTO();
        dto.setIdSuscripcion(suscripcion.getIdSuscripcion());
        dto.setNombreServicio(suscripcion.getNombreServicio());
        dto.setDescripcion(suscripcion.getDescripcion());
        dto.setMonto(suscripcion.getMonto());
        dto.setFrecuencia(suscripcion.getFrecuencia());
        dto.setProximaFechaCobro(suscripcion.getProximaFechaCobro());
        dto.setRecordatorioActivo(suscripcion.getRecordatorioActivo());
        dto.setDiasAnticipacion(suscripcion.getDiasAnticipacion());
        dto.setUltimaFechaRecordatorio(suscripcion.getUltimaFechaRecordatorio());
        dto.setAlertaSaldoActiva(suscripcion.getAlertaSaldoActiva());
        dto.setUltimaFechaAlertaSaldo(suscripcion.getUltimaFechaAlertaSaldo());
        dto.setEstado(suscripcion.getEstado());
        dto.setFechaCancelacion(suscripcion.getFechaCancelacion());
        dto.setFechaCreacion(suscripcion.getFechaCreacion());
        dto.setFechaActualizacion(suscripcion.getFechaActualizacion());
        return dto;
    }
}