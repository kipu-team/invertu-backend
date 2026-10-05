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
    private String mensaje;
}
