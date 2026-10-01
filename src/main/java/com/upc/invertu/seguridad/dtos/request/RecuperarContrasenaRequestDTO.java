package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-03 */
@Getter
@Setter
@NoArgsConstructor
public class RecuperarContrasenaRequestDTO {
    // El regexp exige un dominio con punto (ej. "ana@upc" no es valido)
    @NotBlank(message = "Ingresa un correo válido")
    @Email(regexp = ".+@.+\\..+", message = "Ingresa un correo válido")
    private String correo;
}
