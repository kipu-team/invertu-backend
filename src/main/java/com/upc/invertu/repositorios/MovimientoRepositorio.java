package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepositorio extends JpaRepository<Movimiento, Long> {
    // Los query methods se agregan al implementar los endpoints de su modulo.
}
