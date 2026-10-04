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
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;
    // 1, 3 o 7; obligatorio solo si activo = true, se valida en el Service
    private Integer diasAnticipacion;
}