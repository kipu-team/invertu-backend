package com.upc.invertu.seguridad.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-PROF-03 */
@Getter
@Setter
@NoArgsConstructor
public class PreferenciasRequestDTO {
    private String tema;
    private String idioma;
}
