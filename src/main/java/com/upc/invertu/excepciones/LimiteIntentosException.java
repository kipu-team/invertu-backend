package com.upc.invertu.excepciones;

/** 429 Too Many Requests: se supero el limite de intentos (login, recuperacion, IA). */
public class LimiteIntentosException extends RuntimeException {
    public LimiteIntentosException(String mensaje) {
        super(mensaje);
    }
}
