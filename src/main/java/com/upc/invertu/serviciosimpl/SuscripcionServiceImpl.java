package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.servicios.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepositorio suscripcionRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;

    @Override
    @Transactional
    public SuscripcionResponseDTO crearSuscripcion(SuscripcionRequestDTO request) {
        var estudiante = estudianteRepositorio.findById(request.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEstudiante(estudiante);
        mapearRequestAEntidad(request, suscripcion);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);

        Suscripcion guardada = suscripcionRepositorio.save(suscripcion);
        return mapearEntidadAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SuscripcionResponseDTO obtenerSuscripcionPorId(Long idSuscripcion) {
        return suscripcionRepositorio.findById(idSuscripcion)
                .map(this::mapearEntidadAResponse)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuscripcionResponseDTO> obtenerSuscripcionesPorEstudiante(Long estudianteId) {
        return suscripcionRepositorio.findByEstudianteIdEstudiante(estudianteId)
                .stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SuscripcionResponseDTO actualizarSuscripcion(Long idSuscripcion, SuscripcionRequestDTO request) {
        Suscripcion suscripcion = suscripcionRepositorio.findById(idSuscripcion)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        mapearRequestAEntidad(request, suscripcion);
        return mapearEntidadAResponse(suscripcionRepositorio.save(suscripcion));
    }

    @Override
    @Transactional
    public void cancelarSuscripcion(Long idSuscripcion) {
        Suscripcion suscripcion = suscripcionRepositorio.findById(idSuscripcion)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDate.now());
        suscripcionRepositorio.save(suscripcion);
    }

    @Override
    @Transactional
    public void reactivarSuscripcion(Long idSuscripcion) {
        Suscripcion suscripcion = suscripcionRepositorio.findById(idSuscripcion)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaCancelacion(null);
        suscripcionRepositorio.save(suscripcion);
    }

    private void mapearRequestAEntidad(SuscripcionRequestDTO request, Suscripcion suscripcion) {
        suscripcion.setNombreServicio(request.getNombreServicio());
        suscripcion.setDescripcion(request.getDescripcion());
        suscripcion.setMonto(request.getMonto());
        suscripcion.setFrecuencia(request.getFrecuencia());
        suscripcion.setProximaFechaCobro(request.getProximaFechaCobro());
        suscripcion.setRecordatorioActivo(request.getRecordatorioActivo() != null && request.getRecordatorioActivo());
        suscripcion.setDiasAnticipacion(request.getDiasAnticipacion());
        suscripcion.setAlertaSaldoActiva(request.getAlertaSaldoActiva() != null && request.getAlertaSaldoActiva());
    }

    private SuscripcionResponseDTO mapearEntidadAResponse(Suscripcion suscripcion) {
        SuscripcionResponseDTO response = new SuscripcionResponseDTO();
        response.setIdSuscripcion(suscripcion.getIdSuscripcion());
        response.setNombreServicio(suscripcion.getNombreServicio());
        response.setDescripcion(suscripcion.getDescripcion());
        response.setMonto(suscripcion.getMonto());
        response.setFrecuencia(suscripcion.getFrecuencia());
        response.setProximaFechaCobro(suscripcion.getProximaFechaCobro());
        response.setRecordatorioActivo(suscripcion.getRecordatorioActivo());
        response.setDiasAnticipacion(suscripcion.getDiasAnticipacion());
        response.setAlertaSaldoActiva(suscripcion.getAlertaSaldoActiva());
        response.setEstado(suscripcion.getEstado());
        return response;
    }
}