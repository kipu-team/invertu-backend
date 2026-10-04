package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        // No se crea ningun Movimiento: registrar una suscripcion no genera movimientos

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
    // ===== IMPLEMENTACIÓN DE LA US-27 (END-SUB-03) =====
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
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Estado inválido. Valores permitidos: ACTIVA, CANCELADA");
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

    // ===== IMPLEMENTACIÓN DE LA US-27 (END-SUB-04) =====
    @Override
    @Transactional(readOnly = true)
    public SuscripcionDetalleResponseDTO obtenerDetalle(Long idSuscripcion) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        Suscripcion s = suscripcionRepositorio
                .findByIdSuscripcionAndEstudianteIdEstudiante(idSuscripcion, idEstudiante)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Suscripción no encontrada"));

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

    // ===== IMPLEMENTACIÓN DE LA US-30 (END-SUB-06) =====
    @Override
    @Transactional
    public SuscripcionResponseDTO cancelarSuscripcion(Long id) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        Suscripcion suscripcion = suscripcionRepositorio
                .findByIdSuscripcionAndEstudianteIdEstudiante(id, idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Suscripción no encontrada"));

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo puedes cancelar suscripciones activas");
        }

        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDate.now());
        suscripcion.setRecordatorioActivo(false);
        suscripcion.setAlertaSaldoActiva(false);
        suscripcion.setDiasAnticipacion(null);

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    // ===== IMPLEMENTACIÓN DE LA US-30 (END-SUB-07) =====
    @Override
    @Transactional
    public SuscripcionResponseDTO reactivarSuscripcion(Long id, LocalDate proximaFechaCobro) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        Suscripcion suscripcion = suscripcionRepositorio
                .findByIdSuscripcionAndEstudianteIdEstudiante(id, idEstudiante)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Suscripción no encontrada"));

        if (suscripcion.getEstado() != EstadoSuscripcion.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo puedes reactivar suscripciones canceladas");
        }

        if (proximaFechaCobro.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La próxima fecha de cobro no puede ser anterior a hoy");
        }

        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaCancelacion(null);
        suscripcion.setProximaFechaCobro(proximaFechaCobro);

        return aDTO(suscripcionRepositorio.save(suscripcion));
    }

    // ---------- Métodos Auxiliares para el cálculo ----------

    private boolean calcularPagoSinRegistrar(Suscripcion s) {
        if (s.getEstado() != EstadoSuscripcion.ACTIVA || s.getProximaFechaCobro() == null) return false;

        java.time.LocalDate ultimoCobro = restarPeriodo(s.getProximaFechaCobro(), s.getFrecuencia());
        if (!ultimoCobro.isBefore(java.time.LocalDate.now())) return false;

        return !movimientoRepositorio.existsBySuscripcionIdSuscripcionAndTipoAndFechaGreaterThanEqual(
                s.getIdSuscripcion(), "GASTO", ultimoCobro);
    }

    private java.time.LocalDate restarPeriodo(java.time.LocalDate fecha, FrecuenciaSuscripcion frecuencia) {
        return switch (frecuencia) {
            case SEMANAL    -> fecha.minusWeeks(1);
            case MENSUAL    -> fecha.minusMonths(1);
            case TRIMESTRAL -> fecha.minusMonths(3);
            case SEMESTRAL  -> fecha.minusMonths(6);
            case ANUAL      -> fecha.minusYears(1);
            default -> throw new IllegalStateException("Frecuencia no soportada: " + frecuencia);
        };
    }
}
