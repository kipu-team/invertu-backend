package com.upc.invertu.excepciones;

/** 503 Service Unavailable: el servicio externo de IA no responde (END-CHAT-01). */
public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }
}
