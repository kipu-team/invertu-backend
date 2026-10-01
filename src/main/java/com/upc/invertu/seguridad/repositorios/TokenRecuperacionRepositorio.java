package com.upc.invertu.seguridad.repositorios;

import com.upc.invertu.seguridad.entidades.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRecuperacionRepositorio extends JpaRepository<TokenRecuperacion, Long> {

    /** END-AUTH-03: al pedir un enlace nuevo, los anteriores del estudiante dejan de servir. */
    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true " +
            "WHERE t.estudiante.idEstudiante = :idEstudiante AND t.usado = false")
    int invalidarTokensVigentes(@Param("idEstudiante") Long idEstudiante);
}
