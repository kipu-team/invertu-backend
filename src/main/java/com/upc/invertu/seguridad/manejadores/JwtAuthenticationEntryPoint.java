package com.upc.invertu.seguridad.manejadores;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Peticion sin token, con token invalido o expirado -> 401 "Sesión no válida". */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        RespuestaErrorJson.escribir(response, HttpStatus.UNAUTHORIZED.value(), "Sesión no válida");
    }
}
