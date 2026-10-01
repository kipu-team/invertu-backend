package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-02 */
@Getter
@Setter
@NoArgsConstructor
public class AuthRequestDTO {
    // El regexp exige un dominio con punto (igual que en el registro)
    @NotBlank(message = "El correo y la contraseña son obligatorios")
    @Email(regexp = ".+@.+\\..+", message = "Ingresa un correo válido")
    private String correo;

    @NotBlank(message = "El correo y la contraseña son obligatorios")
    private String contrasena;
}
