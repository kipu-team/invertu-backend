package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.enums.EstadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaRepositorio extends JpaRepository<Meta, Long> {
    /** END-GOAL-01 y 02: cuenta las metas del estudiante en un estado (ACTIVA para el limite del plan). */
    long countByEstudianteIdEstudianteAndEstado(Long idEstudiante, EstadoMeta estado);
}
