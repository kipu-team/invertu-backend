package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Respuesta generica { mensaje } compartida por varios endpoints */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    // TODO: atributos segun la tabla de endpoints del informe (seccion 2.3)
    private String mensaje;
}
