package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositorio extends JpaRepository<Suscripcion, Long> {

    // Los query methods se agregan al implementar los endpoints de su modulo.
    /** END-TRX-02 y 05: la suscripcion debe ser del estudiante y estar en el estado indicado (ACTIVA). */
    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudianteAndEstado(
            Long idSuscripcion, Long idEstudiante, EstadoSuscripcion estado);

    List<Suscripcion> findByEstudianteIdEstudianteOrderByProximaFechaCobroAsc(Long idEstudiante);
    List<Suscripcion> findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(Long idEstudiante, EstadoSuscripcion estado);
    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudiante(Long idSuscripcion, Long idEstudiante);
    long countByEstudianteIdEstudianteAndEstadoAndRecordatorioActivoTrue(Long idEstudiante, EstadoSuscripcion estado);
}
