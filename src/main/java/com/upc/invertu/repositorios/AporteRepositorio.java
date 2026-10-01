package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Aporte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AporteRepositorio extends JpaRepository<Aporte, Long> {
    // Los query methods se agregan al implementar los endpoints de su modulo.
}
