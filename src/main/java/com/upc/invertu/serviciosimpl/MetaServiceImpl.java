package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.MetaRequestDTO;
import com.upc.invertu.dtos.response.*;
import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.Plan;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.FrecuenciaAporte;
import com.upc.invertu.excepciones.LimitePlanException;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.MetaService;
import com.upc.invertu.utilidades.CalculosMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MetaServiceImpl implements MetaService {

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private PlanRepositorio planRepositorio;

    @Autowired
    private AporteRepositorio aporteRepositorio;

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
        meta.setProximaFechaAporte(CalculosMeta.proximaFechaAporte(frecuencia, dto.getFechaObjetivo()));
        return aDTO(metaRepositorio.save(meta), BigDecimal.ZERO);
    }

    /** END-GOAL-02: indica si el estudiante alcanzo el limite de metas activas de su plan */
    @Override
    @Transactional(readOnly = true)
    public EstadoLimiteResponseDTO consultarEstadoLimite() {
        return calcularEstadoLimite(estudianteAutenticado.obtener());
    }

    /** END-GOAL-03: metas ACTIVA, de la fecha objetivo mas proxima a la mas lejana, con su progreso */
    @Override
    @Transactional(readOnly = true)
    public List<MetaResumenResponseDTO> listarActivas() {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        List<Meta> metas = metaRepositorio.findByEstudianteIdEstudianteAndEstadoOrderByFechaObjetivoAsc(
                idEstudiante, EstadoMeta.ACTIVA);
        Map<Long, BigDecimal> aportado = obtenerAportadoPorMeta(idEstudiante);
        LocalDate hoy = LocalDate.now();

        return metas.stream().map(meta -> {
            BigDecimal montoAportado = aportado.getOrDefault(meta.getIdMeta(), BigDecimal.ZERO);
            MetaResumenResponseDTO dto = aResumenDTO(meta, montoAportado);
            dto.setPorcentaje(CalculosMeta.porcentaje(montoAportado, meta.getMontoObjetivo()));
            dto.setFechaObjetivo(meta.getFechaObjetivo());
            dto.setVencida(meta.getFechaObjetivo().isBefore(hoy));
            return dto;
        }).toList();
    }

    /** END-GOAL-04: metas CUMPLIDA y CANCELADA, de la mas reciente a la mas antigua */
    @Override
    @Transactional(readOnly = true)
    public List<MetaResumenResponseDTO> listarFinalizadas() {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        List<Meta> metas = metaRepositorio.findByEstudianteIdEstudianteAndEstadoInOrderByFechaActualizacionDesc(
                idEstudiante, List.of(EstadoMeta.CUMPLIDA, EstadoMeta.CANCELADA));
        Map<Long, BigDecimal> aportado = obtenerAportadoPorMeta(idEstudiante);

        return metas.stream().map(meta -> {
            MetaResumenResponseDTO dto = aResumenDTO(meta,
                    aportado.getOrDefault(meta.getIdMeta(), BigDecimal.ZERO));
            dto.setEstado(meta.getEstado());
            dto.setFechaCumplimiento(meta.getFechaCumplimiento());
            return dto;
        }).toList();
    }
    /** END-GOAL-05: detalle de una meta propia con su progreso */
    @Override
    @Transactional(readOnly = true)
    public MetaDetalleResponseDTO obtenerDetalle(Long idMeta) {
        Meta meta = buscarPropia(idMeta);
        BigDecimal aportado = sumarAportes(idMeta);

        MetaDetalleResponseDTO dto = new MetaDetalleResponseDTO();
        dto.setIdMeta(meta.getIdMeta());
        dto.setNombre(meta.getNombre());
        dto.setDescripcion(meta.getDescripcion());
        dto.setMontoObjetivo(meta.getMontoObjetivo());
        dto.setMontoAportado(aportado);
        dto.setPorcentaje(CalculosMeta.porcentaje(aportado, meta.getMontoObjetivo()));
        dto.setFechaObjetivo(meta.getFechaObjetivo());
        dto.setFrecuenciaAporte(meta.getFrecuenciaAporte());
        dto.setProximaFechaAporte(meta.getProximaFechaAporte());
        dto.setEstado(meta.getEstado());
        // Solo una meta ACTIVA puede estar vencida; las CUMPLIDA y CANCELADA ya terminaron
        dto.setVencida(meta.getEstado() == EstadoMeta.ACTIVA
                && meta.getFechaObjetivo().isBefore(LocalDate.now()));
        return dto;
    }

    /**
     * END-GOAL-07 (solo Premium, validado en el controller):
     * estima cuando se cumplira la meta si el estudiante sigue aportando su promedio con su frecuencia.
     */
    @Override
    @Transactional(readOnly = true)
    public ProyeccionResponseDTO obtenerProyeccion(Long idMeta) {
        Meta meta = buscarPropia(idMeta);

        if (meta.getEstado() == EstadoMeta.CUMPLIDA) {
            return new ProyeccionResponseDTO(meta.getFechaCumplimiento(), "La meta ya fue cumplida");
        }
        if (meta.getEstado() == EstadoMeta.CANCELADA) {
            return new ProyeccionResponseDTO(null, "La meta está cancelada");
        }

        int cantidadAportes = aporteRepositorio.findByMetaIdMetaOrderByFechaAsc(idMeta).size();
        if (cantidadAportes == 0) {
            return new ProyeccionResponseDTO(null, "Aún no hay aportes suficientes para estimar");
        }
        if (meta.getFrecuenciaAporte() == FrecuenciaAporte.SIN_FRECUENCIA) {
            return new ProyeccionResponseDTO(null, "Define una frecuencia de aporte para estimar la fecha");
        }

        BigDecimal aportado = sumarAportes(idMeta);
        BigDecimal restante = meta.getMontoObjetivo().subtract(aportado);
        BigDecimal promedio = aportado.divide(BigDecimal.valueOf(cantidadAportes), 2, RoundingMode.HALF_UP);

        // Cuantos aportes promedio faltan; se redondea hacia arriba (si falta medio aporte, es un aporte mas)
        int aportesFaltantes = restante.divide(promedio, 0, RoundingMode.CEILING).intValue();
        LocalDate fechaEstimada = sumarPeriodos(LocalDate.now(), meta.getFrecuenciaAporte(), aportesFaltantes);

        String mensaje = fechaEstimada.isAfter(meta.getFechaObjetivo())
                ? "Al ritmo actual cumplirías tu meta después de la fecha objetivo"
                : "Al ritmo actual cumplirías tu meta a tiempo";
        return new ProyeccionResponseDTO(fechaEstimada, mensaje);
    }

    /**
     * END-GOAL-08: edita una meta propia ACTIVA.
     * Solo cambian nombre, montoObjetivo, fechaObjetivo, frecuenciaAporte y descripcion;
     * el dueno, los aportes, el estado y la fecha de creacion no se tocan.
     */
    @Override
    @Transactional
    public MetaResponseDTO actualizar(Long idMeta, MetaRequestDTO dto) {
        // 404 si la meta no existe o es de otro estudiante
        Meta meta = buscarPropia(idMeta);

        // Solo se puede editar una meta que sigue en curso
        if (meta.getEstado() != EstadoMeta.ACTIVA) {
            throw new ReglaNegocioException("Datos inválidos");
        }

        BigDecimal aportado = sumarAportes(idMeta);
        if (dto.getMontoObjetivo().compareTo(aportado) <= 0) {
            throw new ReglaNegocioException("El monto objetivo debe ser mayor a lo ya ahorrado");
        }

        // Si no viene la frecuencia en el request se conserva la actual: en edicion no aplica
        // el default SEMANAL de la creacion, para no cambiarle la frecuencia a la meta por accidente
        FrecuenciaAporte frecuencia = dto.getFrecuenciaAporte() != null
                ? dto.getFrecuenciaAporte()
                : meta.getFrecuenciaAporte();

        // Se detectan los cambios antes de tocar la entidad (con los valores actuales)
        boolean cambioFrecuencia = meta.getFrecuenciaAporte() != frecuencia;
        boolean cambioFechaObjetivo = !dto.getFechaObjetivo().equals(meta.getFechaObjetivo());

        meta.setNombre(dto.getNombre().trim());
        meta.setDescripcion(limpiarTexto(dto.getDescripcion()));
        meta.setMontoObjetivo(dto.getMontoObjetivo());
        meta.setFechaObjetivo(dto.getFechaObjetivo());
        meta.setFrecuenciaAporte(frecuencia);
        // Si cambio la frecuencia o la fecha objetivo, la proximaFechaAporte se recalcula con la
        // frecuencia final y la nueva fechaObjetivo, para no quedar en una fecha posterior a la meta
        if (cambioFrecuencia || cambioFechaObjetivo) {
            meta.setProximaFechaAporte(CalculosMeta.proximaFechaAporte(frecuencia, meta.getFechaObjetivo()));
        }
        return aDTO(metaRepositorio.save(meta), aportado);
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

    // Convierte las filas [idMeta, suma] de aportadoPorMeta en un Map para buscar rapido por idMeta
    private Map<Long, BigDecimal> obtenerAportadoPorMeta(Long idEstudiante) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        for (Object[] fila : aporteRepositorio.aportadoPorMeta(idEstudiante)) {
            mapa.put((Long) fila[0], (BigDecimal) fila[1]);
        }
        return mapa;
    }

    // Campos comunes a END-GOAL-03 y 04
    private MetaResumenResponseDTO aResumenDTO(Meta meta, BigDecimal montoAportado) {
        MetaResumenResponseDTO dto = new MetaResumenResponseDTO();
        dto.setIdMeta(meta.getIdMeta());
        dto.setNombre(meta.getNombre());
        dto.setMontoObjetivo(meta.getMontoObjetivo());
        dto.setMontoAportado(montoAportado);
        return dto;
    }

    /** Busca una meta solo si es del estudiante autenticado; si no, 404. */
    private Meta buscarPropia(Long idMeta) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        return metaRepositorio.findByIdMetaAndEstudianteIdEstudiante(idMeta, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("La meta no existe"));
    }

    // SUM devuelve null cuando la meta no tiene aportes: en ese caso el total es 0
    private BigDecimal sumarAportes(Long idMeta) {
        BigDecimal suma = aporteRepositorio.sumarAportes(idMeta);
        return suma != null ? suma : BigDecimal.ZERO;
    }

    // Avanza una fecha la cantidad de periodos indicada segun la frecuencia
    private LocalDate sumarPeriodos(LocalDate desde, FrecuenciaAporte frecuencia, int periodos) {
        return switch (frecuencia) {
            case DIARIA -> desde.plusDays(periodos);
            case SEMANAL -> desde.plusWeeks(periodos);
            case MENSUAL -> desde.plusMonths(periodos);
            case SIN_FRECUENCIA -> desde;
        };
    }

}