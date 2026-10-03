package com.upc.invertu.seguridad.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-PROF-04 */
@Getter
@Setter
@NoArgsConstructor
public class CambiarContrasenaRequestDTO {
    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;
}
