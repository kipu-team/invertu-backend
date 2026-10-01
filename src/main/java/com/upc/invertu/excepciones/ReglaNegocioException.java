package com.upc.invertu.excepciones;

/** 400 Bad Request: el dato es valido pero incumple una regla de negocio. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
