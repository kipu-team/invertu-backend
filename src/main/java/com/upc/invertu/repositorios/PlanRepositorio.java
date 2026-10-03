package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepositorio extends JpaRepository<Plan, Long> {

    Optional<Plan> findByRolNombre(String nombreRol);

    boolean existsByRolNombre(String nombreRol);

    /** END-PROF-01: plan correspondiente al rol del estudiante. */
    Optional<Plan> findByRolIdRol(Long idRol);
}
