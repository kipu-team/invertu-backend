package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositorio extends JpaRepository<Suscripcion, Long> {

    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudianteAndEstado(
            Long idSuscripcion, Long idEstudiante, EstadoSuscripcion estado);

    // AGREGAR ESTA LÍNEA:
    List<Suscripcion> findByEstudianteIdEstudiante(Long idEstudiante);
}