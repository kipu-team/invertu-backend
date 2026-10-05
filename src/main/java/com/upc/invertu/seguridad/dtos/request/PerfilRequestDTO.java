package com.upc.invertu.seguridad.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-PROF-02 */
@Getter
@Setter
@NoArgsConstructor
public class PerfilRequestDTO {
    // @NotBlank rechaza vacios y solo espacios
    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 100, message = "Los nombres pueden tener como máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Este campo es obligatorio")
    @Size(max = 100, message = "Los apellidos pueden tener como máximo 100 caracteres")
    private String apellidos;

    @Size(max = 150, message = "La universidad puede tener como máximo 150 caracteres")
    private String universidad;
}
