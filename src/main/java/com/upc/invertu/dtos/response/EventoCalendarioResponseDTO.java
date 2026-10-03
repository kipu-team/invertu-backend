package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.TipoEvento;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** END-CAL-01 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //Hace que en la respuesta de un dia no se repita en cada evento
public class EventoCalendarioResponseDTO {
    private LocalDate fecha;
    private TipoEvento tipo; // META_LIMITE, META_APORTE o SUSCRIPCION
    private Long idOrigen; // idMeta o idSuscripcion, para el boton "Ver detalle"
    private String nombre; // nombre de la meta o del servicio
    private BigDecimal monto;
}
