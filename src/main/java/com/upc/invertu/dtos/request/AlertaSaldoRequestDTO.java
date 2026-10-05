package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-SUB-09 */
@Getter
@Setter
@NoArgsConstructor
public class AlertaSaldoRequestDTO {
    @NotNull(message = "El campo activa es obligatorio")
    private Boolean activa;
}
