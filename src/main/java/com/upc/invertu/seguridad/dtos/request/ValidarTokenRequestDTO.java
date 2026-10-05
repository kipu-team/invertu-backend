package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-04 */
@Getter
@Setter
@NoArgsConstructor
public class ValidarTokenRequestDTO {
    // Va en el body (no en la URL) para que no quede en logs ni en el historial
    @NotBlank(message = "Este campo es obligatorio")
    private String token;
}
