package com.upc.invertu.seguridad.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-01 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponseDTO {
    private Long idEstudiante;
    private String nombres;
    private String correo;
    private String rol;
}
