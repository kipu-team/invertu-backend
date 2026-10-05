package com.upc.invertu.excepciones;

/** 409 Conflict: por ejemplo, correo ya registrado. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
