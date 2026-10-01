package com.upc.invertu.excepciones;

/** Formato unico de error de la API: { "error": "mensaje" }. */
public record ErrorResponseDTO(String error) {
}
