package com.upc.invertu.seguridad.repositorios;

import com.upc.invertu.seguridad.entidades.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRecuperacionRepositorio extends JpaRepository<TokenRecuperacion, Long> {
    // Los query methods se agregan al implementar los endpoints de su modulo.
}
