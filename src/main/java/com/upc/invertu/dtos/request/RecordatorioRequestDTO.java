package com.upc.invertu.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-SUB-08 */
@Getter
@Setter
@NoArgsConstructor
public class RecordatorioRequestDTO {
    // TODO: atributos segun la tabla de endpoints del informe (seccion 2.3)
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;

    private Integer diasAnticipacion;
}
