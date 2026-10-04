package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

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
}
