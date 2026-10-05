package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-05 */
@Getter
@Setter
@NoArgsConstructor
public class RestablecerContrasenaRequestDTO {
    @NotBlank(message = "Este campo es obligatorio")
    private String token;

    // Mismos requisitos que en el registro (END-AUTH-01)
    @NotBlank(message = "Este campo es obligatorio")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
    @Size(max = 72, message = "La contraseña puede tener como máximo 72 caracteres") // limite de BCrypt
    private String nuevaContrasena;

    @NotBlank(message = "Este campo es obligatorio")
    private String confirmarContrasena;
}
