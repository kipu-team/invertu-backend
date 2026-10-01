package com.upc.invertu.excepciones;

/** 422 Unprocessable Entity: la IA no pudo leer el comprobante (END-TRX-03). */
public class ArchivoNoProcesableException extends RuntimeException {
    public ArchivoNoProcesableException(String mensaje) {
        super(mensaje);
    }
}
