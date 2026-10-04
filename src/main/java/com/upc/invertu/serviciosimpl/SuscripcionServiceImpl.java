package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.AlertaSaldoRequestDTO;
import com.upc.invertu.dtos.request.RecordatorioRequestDTO;
import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.LimitePlanException;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-SUB-01: registra una suscripcion del estudiante autenticado en estado ACTIVA */
    @Override
    @Transactional
    public SuscripcionResponseDTO registrar(SuscripcionRequestDTO dto) {
        // El estudiante sale del token, nunca del request
        Estudiante estudiante = estudianteAutenticado.obtener();

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEstudiante(estudiante);
        suscripcion.setNombreServicio(dto.getNombreServicio().trim());
        suscripcion.setDescripcion(dto.getDescripcion());
        suscripcion.setMonto(dto.getMonto());
        suscripcion.setFrecuencia(dto.getFrecuencia());
        suscripcion.setProximaFechaCobro(dto.getProximaFechaCobro());
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setRecordatorioActivo(false);   // queda desactivado hasta que el estudiante lo configure
        suscripcion.setAlertaSaldoActiva(false);    // idem para la alerta de saldo

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarFrecuencias() {
        return List.of(FrecuenciaSuscripcion.values()).stream()
                .map(Enum::name)
                .toList();
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

    // Implementacion de la US-27 END-SUB-03
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
                        calcularPagoSinRegistrar(s)))
                .toList();
    }
    // Implementacion de la US-27 END-SUB-04

    @Override
    @Transactional(readOnly = true)
    public SuscripcionDetalleResponseDTO obtenerDetalle(Long idSuscripcion) {
        Suscripcion s = buscarPropia(idSuscripcion);

        List<SuscripcionDetalleResponseDTO.PagoHistorialDTO> pagos = movimientoRepositorio
                .findBySuscripcionIdSuscripcionOrderByFechaDesc(s.getIdSuscripcion())
                .stream()
                .map(m -> new SuscripcionDetalleResponseDTO.PagoHistorialDTO(
                        m.getFecha(),
                        m.getMonto(),
                        m.getMedioPago() != null ? m.getMedioPago().name() : "OTRO"))
                .toList();

        return new SuscripcionDetalleResponseDTO(
                s.getIdSuscripcion(),
                s.getNombreServicio(),
                s.getDescripcion(),
                s.getMonto(),
                String.valueOf(s.getFrecuencia()),
                s.getProximaFechaCobro(),
                String.valueOf(s.getEstado()),
                s.getFechaCreacion(),
                calcularPagoSinRegistrar(s),
                pagos);
    }

    // Implementacion de la US-30 END-SUB-06
    @Override
    @Transactional
    public SuscripcionResponseDTO cancelarSuscripcion(Long id) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {throw new ReglaNegocioException("Solo puedes cancelar suscripciones activas");}

        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDate.now());
        suscripcion.setRecordatorioActivo(false);
        suscripcion.setAlertaSaldoActiva(false);
        suscripcion.setDiasAnticipacion(null);

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    // Implementacion de la US-30 END-SUB-07
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

    // metodos auxiliares para el cálculo

    /** END-SUB-03 y 04: hay un pago sin registrar si la suscripcion esta ACTIVA, su ultimo cobro (proximaFechaCobro menos un periodo) ya paso, es igual o posterior a la fecha en que se creo
     * la suscripcion, y no hay un pago (GASTO) con fecha igual o posterior a ese cobro */
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

    /** Busca una suscripcion solo si es del estudiante autenticado si no, 404. */
    private Suscripcion buscarPropia(Long idSuscripcion) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        return suscripcionRepositorio.findByIdSuscripcionAndEstudianteIdEstudiante(idSuscripcion, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("La suscripción no existe"));
    }

    @Override
    @Transactional
    public SuscripcionResponseDTO editarSuscripcion(Long id, SuscripcionRequestDTO request) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden editar suscripciones activas");
        }

        suscripcion.setNombreServicio(request.getNombreServicio());
        suscripcion.setDescripcion(request.getDescripcion());
        suscripcion.setMonto(request.getMonto());
        suscripcion.setFrecuencia(request.getFrecuencia());
        suscripcion.setProximaFechaCobro(request.getProximaFechaCobro());

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    // US-31 (T-49 / END-SUB-08)
    @Override
    @Transactional
    public SuscripcionResponseDTO configurarRecordatorio(Long id, RecordatorioRequestDTO request) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden configurar suscripciones activas");
        }

        if (Boolean.TRUE.equals(request.getActivo())) {
            if (request.getDiasAnticipacion() == null || !List.of(1, 3, 7).contains(request.getDiasAnticipacion())) {
                throw new ReglaNegocioException("Días de anticipación no válidos");
            }

            // Validar límite para rol FREE (máximo 3 activos)
            boolean esFree = SecurityContextHolder.getContext()
                    .getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("FREE") || a.getAuthority().contains("ROLE_FREE"));

            if (esFree) {
                long activos = suscripcionRepositorio.countByEstudianteIdEstudianteAndEstadoAndRecordatorioActivoTrue(idEstudiante, EstadoSuscripcion.ACTIVA);
                if (activos >= 3 && !Boolean.TRUE.equals(suscripcion.getRecordatorioActivo())) {
                    throw new LimitePlanException("Con el plan Free puedes activar recordatorios en hasta 3 suscripciones");
                }
            }

            suscripcion.setRecordatorioActivo(true);
            suscripcion.setDiasAnticipacion(request.getDiasAnticipacion());
        } else {
            suscripcion.setRecordatorioActivo(false);
            suscripcion.setDiasAnticipacion(null);
        }

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    // US-32 (T-51 / END-SUB-09)
    @Override
    @Transactional
    public SuscripcionResponseDTO configurarAlertaSaldo(Long id, AlertaSaldoRequestDTO request) {
        Suscripcion suscripcion = buscarPropia(id);

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {throw new ReglaNegocioException("Solo se pueden configurar suscripciones activas");}

        suscripcion.setAlertaSaldoActiva(request.getActiva());
        if (Boolean.FALSE.equals(request.getActiva())) {suscripcion.setUltimaFechaAlertaSaldo(null);}

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }
}