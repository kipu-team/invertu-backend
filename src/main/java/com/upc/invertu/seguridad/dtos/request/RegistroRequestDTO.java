package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-01 */
@Getter
@Setter
@NoArgsConstructor
public class RegistroRequestDTO {
    // @NotBlank rechaza vacios y solo espacios
    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 100, message = "Los nombres pueden tener como máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 100, message = "Los apellidos pueden tener como máximo 100 caracteres")
    private String apellidos;

    // El regexp exige un dominio con punto (ej. "ana@upc" no es valido)
    @NotBlank(message = "Este campo es obligatorio")
    @Email(regexp = ".+@.+\\..+", message = "Ingresa un correo válido")
    @Size(max = 150, message = "El correo puede tener como máximo 150 caracteres")
    private String correo;

    // Minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial
    @NotBlank(message = "Este campo es obligatorio")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
    @Size(max = 72, message = "La contraseña puede tener como máximo 72 caracteres") // limite de BCrypt
    private String contrasena;

    @NotBlank(message = "Este campo es obligatorio")
    private String confirmarContrasena;
}
