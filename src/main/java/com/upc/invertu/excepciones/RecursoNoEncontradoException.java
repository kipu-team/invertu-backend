package com.upc.invertu.excepciones;

/** 404 Not Found: el recurso no existe o no pertenece al estudiante autenticado. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
