package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositorio extends JpaRepository<Suscripcion, Long> {

    /** END-CAL-01: suscripciones de un estado (ACTIVA), del cobro mas proximo al mas lejano. */
    List<Suscripcion> findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(
            Long idEstudiante, EstadoSuscripcion estado);

    // Los query methods se agregan al implementar los endpoints de su modulo.
    /** END-TRX-02 y 05: la suscripcion debe ser del estudiante y estar en el estado indicado (ACTIVA). */
    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudianteAndEstado(
            Long idSuscripcion, Long idEstudiante, EstadoSuscripcion estado);

    List<Suscripcion> findByEstudianteIdEstudianteOrderByProximaFechaCobroAsc(Long idEstudiante);
    List<Suscripcion> findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(Long idEstudiante, String estado);

    Optional<Suscripcion> findByIdSuscripcionAndEstudianteTdEstudiante(Long idSuscripcion, Long idEstudiante);
}
