package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.MetaRequestDTO;
import com.upc.invertu.dtos.response.EstadoLimiteResponseDTO;
import com.upc.invertu.dtos.response.MetaResponseDTO;
import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.Plan;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.FrecuenciaAporte;
import com.upc.invertu.excepciones.LimitePlanException;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class MetaServiceImpl implements MetaService {

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private PlanRepositorio planRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-GOAL-01: crea una meta ACTIVA, respetando el limite de metas del plan */
    @Override
    @Transactional
    public MetaResponseDTO crear(MetaRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        // El backend vuelve a validar el limite aunque la interfaz ya lo haya consultado con END-GOAL-02
        if (calcularEstadoLimite(estudiante).isLimiteAlcanzado()) {
            throw new LimitePlanException("Límite del Plan Free alcanzado");
        }

        // Si no se envia la frecuencia, se usa SEMANAL (valor por defecto de la US-18)
        FrecuenciaAporte frecuencia = dto.getFrecuenciaAporte() != null
                ? dto.getFrecuenciaAporte()
                : FrecuenciaAporte.SEMANAL;

        Meta meta = new Meta();
        meta.setEstudiante(estudiante);
        meta.setNombre(dto.getNombre().trim());
        meta.setDescripcion(limpiarTexto(dto.getDescripcion()));
        meta.setMontoObjetivo(dto.getMontoObjetivo());
        meta.setFechaObjetivo(dto.getFechaObjetivo());
        meta.setFrecuenciaAporte(frecuencia);
        meta.setProximaFechaAporte(calcularProximaFechaAporte(frecuencia, dto.getFechaObjetivo()));
        // El estado ACTIVA ya viene por defecto en la entidad Meta

        // Una meta recien creada no tiene aportes
        return aDTO(metaRepositorio.save(meta), BigDecimal.ZERO);
    }

    /** END-GOAL-02: indica si el estudiante alcanzo el limite de metas activas de su plan */
    @Override
    @Transactional(readOnly = true)
    public EstadoLimiteResponseDTO consultarEstadoLimite() {
        return calcularEstadoLimite(estudianteAutenticado.obtener());
    }

    /**
     * Compara las metas ACTIVA del estudiante con el limite de su plan.
     * Se usa en END-GOAL-01 (para bloquear) y en END-GOAL-02 (para informar).
     */
    private EstadoLimiteResponseDTO calcularEstadoLimite(Estudiante estudiante) {
        String rol = estudiante.getRol().getNombre();
        Plan plan = planRepositorio.findByRolNombre(rol)
                .orElseThrow(() -> new IllegalStateException("No existe un plan para el rol " + rol));

        // Se cuenta en cada consulta: las metas CUMPLIDA y CANCELADA no cuentan, asi el cupo se libera solo
        long metasActivas = metaRepositorio.countByEstudianteIdEstudianteAndEstado(
                estudiante.getIdEstudiante(), EstadoMeta.ACTIVA);
        Integer limite = plan.getMaxMetasActivas(); // null = sin limite (Premium)

        EstadoLimiteResponseDTO dto = new EstadoLimiteResponseDTO();
        dto.setRol(rol);
        dto.setMetasActivas((int) metasActivas);
        dto.setLimite(limite);
        dto.setLimiteAlcanzado(limite != null && metasActivas >= limite);
        return dto;
    }

    // Siguiente fecha en que el estudiante deberia aportar; nunca despues de la fecha objetivo
    private LocalDate calcularProximaFechaAporte(FrecuenciaAporte frecuencia, LocalDate fechaObjetivo) {
        LocalDate hoy = LocalDate.now();
        LocalDate proxima = switch (frecuencia) {
            case DIARIA -> hoy.plusDays(1);
            case SEMANAL -> hoy.plusWeeks(1);
            case MENSUAL -> hoy.plusMonths(1);
            case SIN_FRECUENCIA -> null;
        };
        if (proxima != null && proxima.isAfter(fechaObjetivo)) {
            return fechaObjetivo;
        }
        return proxima;
    }

    // Texto opcional: si viene vacio o con solo espacios se guarda null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private MetaResponseDTO aDTO(Meta meta, BigDecimal montoAportado) {
        MetaResponseDTO dto = new MetaResponseDTO();
        dto.setIdMeta(meta.getIdMeta());
        dto.setNombre(meta.getNombre());
        dto.setMontoObjetivo(meta.getMontoObjetivo());
        dto.setMontoAportado(montoAportado);
        dto.setFechaObjetivo(meta.getFechaObjetivo());
        dto.setEstado(meta.getEstado());
        return dto;
    }
}