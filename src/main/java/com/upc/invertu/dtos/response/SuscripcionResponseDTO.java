package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SuscripcionResponseDTO {
    private Long idSuscripcion;
    private String nombreServicio;
    private String descripcion;
    private BigDecimal monto;
    private FrecuenciaSuscripcion frecuencia;
    private LocalDate proximaFechaCobro;
    private Boolean recordatorioActivo;
    private Integer diasAnticipacion;
    private Boolean alertaSaldoActiva;
    private EstadoSuscripcion estado;
}
