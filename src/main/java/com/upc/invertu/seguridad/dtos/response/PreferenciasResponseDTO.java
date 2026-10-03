package com.upc.invertu.seguridad.dtos.response;

import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-PROF-03 */
@Getter
@Setter
@NoArgsConstructor
public class PreferenciasResponseDTO {
    private Tema tema;
    private Idioma idioma;
}
