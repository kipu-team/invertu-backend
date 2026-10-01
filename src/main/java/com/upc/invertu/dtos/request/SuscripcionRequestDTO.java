package com.upc.invertu.dtos.request;

import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SuscripcionRequestDTO {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 100)
    private String nombreServicio;

    @Size(max = 500)
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "La frecuencia es obligatoria")
    private FrecuenciaSuscripcion frecuencia;

    @NotNull(message = "La próxima fecha de cobro es obligatoria")
    private LocalDate proximaFechaCobro;

    private Boolean recordatorioActivo = false;

    private Integer diasAnticipacion;

    private Boolean alertaSaldoActiva = false;
}