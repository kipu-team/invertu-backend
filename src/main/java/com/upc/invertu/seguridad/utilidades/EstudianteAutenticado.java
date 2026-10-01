package com.upc.invertu.seguridad.utilidades;

import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Obtiene al estudiante autenticado a partir del token.
 * Los endpoints nunca reciben idEstudiante en el request: siempre se usa este componente,
 * asi cada estudiante solo puede ver o modificar su propia informacion.
 */
@Component
public class EstudianteAutenticado {

    private final EstudianteRepositorio estudianteRepositorio;

    public EstudianteAutenticado(EstudianteRepositorio estudianteRepositorio) {
        this.estudianteRepositorio = estudianteRepositorio;
    }

    public String obtenerCorreo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public Estudiante obtener() {
        return estudianteRepositorio.findByCorreoConRol(obtenerCorreo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
    }
}
