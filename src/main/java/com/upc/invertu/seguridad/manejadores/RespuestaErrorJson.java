package com.upc.invertu.seguridad.manejadores;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

/** Escribe el error con el mismo formato de la API: { "error": "..." }. */
final class RespuestaErrorJson {

    private RespuestaErrorJson() {
    }

    static void escribir(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
