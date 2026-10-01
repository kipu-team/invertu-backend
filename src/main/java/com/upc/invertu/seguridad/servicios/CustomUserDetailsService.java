package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Carga al estudiante por su correo (que hace de username) junto con su rol.
 * Lo usan el AuthenticationManager (login) y el JwtRequestFilter (cada peticion).
 * Como el rol se lee de la BD en cada peticion, al pasar a Premium el cambio aplica de inmediato.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EstudianteRepositorio estudianteRepositorio;

    public CustomUserDetailsService(EstudianteRepositorio estudianteRepositorio) {
        this.estudianteRepositorio = estudianteRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Estudiante estudiante = estudianteRepositorio
                .findByCorreoConRol(correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Estudiante no encontrado"));

        return User.withUsername(estudiante.getCorreo())
                .password(estudiante.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(estudiante.getRol().getNombre())))
                .build();
    }
}
