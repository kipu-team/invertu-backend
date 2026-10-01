package com.upc.invertu.seguridad.repositorios;

import com.upc.invertu.seguridad.entidades.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EstudianteRepositorio extends JpaRepository<Estudiante, Long> {

    /** END-AUTH-01: verificar si el correo ya esta registrado (se guarda en minusculas). */
    boolean existsByCorreo(String correo);

    /** END-AUTH-02: usado por CustomUserDetailsService para cargar al estudiante con su rol. */
    @Query("SELECT e FROM Estudiante e JOIN FETCH e.rol WHERE e.correo = :correo")
    Optional<Estudiante> findByCorreoConRol(@Param("correo") String correo);
}
