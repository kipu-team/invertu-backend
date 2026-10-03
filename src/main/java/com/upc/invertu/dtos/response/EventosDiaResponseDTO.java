package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/** END-CAL-02 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventosDiaResponseDTO {
    private LocalDate fecha;
    private int total;  // cantidad de eventos ("[N] eventos programados")
    private List<EventoCalendarioResponseDTO> eventos; //lista vacia cuando no hay eventos
}
