package com.upc.invertu.seguridad.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-04 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidarTokenResponseDTO {
    private boolean valido;
    private String mensaje;
}
