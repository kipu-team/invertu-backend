package com.upc.invertu.seguridad.manejadores;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Autenticado pero sin permiso a nivel de filtros -> 403.
 * Nota: el 403 de @PreAuthorize (Free en funcion Premium) lo resuelve GlobalExceptionHandler.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        RespuestaErrorJson.escribir(response, HttpStatus.FORBIDDEN.value(),
                "Esta función es exclusiva del plan Premium");
    }
}
