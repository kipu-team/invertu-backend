package com.upc.invertu.excepciones;

/** 403 Forbidden: el estudiante alcanzo el limite de su plan (END-GOAL-01, END-GOAL-14, END-SUB-08). */
public class LimitePlanException extends RuntimeException {
    public LimitePlanException(String mensaje) {
        super(mensaje);
    }
}
