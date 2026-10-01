package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaRepositorio extends JpaRepository<Meta, Long> {
    // Los query methods se agregan al implementar los endpoints de su modulo.
}
