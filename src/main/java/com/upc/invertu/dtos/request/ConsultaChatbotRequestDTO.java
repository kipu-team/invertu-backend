package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-CHAT-01 */
@Getter
@Setter
@NoArgsConstructor
public class ConsultaChatbotRequestDTO {
    // @NotBlank rechaza null, vacio y solo espacios
    @NotBlank(message = "Ingresa una pregunta")
    @Size(max = 500, message = "La pregunta puede tener como máximo 500 caracteres")
    private String pregunta;
}
