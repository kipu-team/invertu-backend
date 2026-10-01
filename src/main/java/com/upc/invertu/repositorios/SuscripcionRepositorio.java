package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuscripcionRepositorio extends JpaRepository<Suscripcion, Long> {
    // Los query methods se agregan al implementar los endpoints de su modulo.
}
