package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepositorio extends JpaRepository<Plan, Long> {

    Optional<Plan> findByRolNombre(String nombreRol);

    boolean existsByRolNombre(String nombreRol);
}
