package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-CAT-02 */
@Getter
@Setter
@NoArgsConstructor
public class CategoriaRequestDTO {
    @NotBlank(message = "Ingresa un nombre")
    @Size(max = 80, message = "El nombre puede tener como máximo 80 caracteres")
    private String nombre;
}
