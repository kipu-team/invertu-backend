package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.enums.EstadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepositorio extends JpaRepository<Meta, Long> {
    /** END-GOAL-01 y 02: cuenta las metas del estudiante en un estado (ACTIVA para el limite del plan). */
    long countByEstudianteIdEstudianteAndEstado(Long idEstudiante, EstadoMeta estado);
    /** END-GOAL-03: metas de un estado (ACTIVA), de la fecha objetivo mas cercana a la mas lejana. */
    List<Meta> findByEstudianteIdEstudianteAndEstadoOrderByFechaObjetivoAsc(Long idEstudiante, EstadoMeta estado);

    /** END-GOAL-04: metas en varios estados (CUMPLIDA y CANCELADA), de la mas reciente a la mas antigua. */
    List<Meta> findByEstudianteIdEstudianteAndEstadoInOrderByFechaActualizacionDesc(Long idEstudiante, List<EstadoMeta> estados);

    /** END-GOAL-05, 06 y 07: solo encuentra la meta si pertenece al estudiante. */
    Optional<Meta> findByIdMetaAndEstudianteIdEstudiante(Long idMeta, Long idEstudiante);
}
