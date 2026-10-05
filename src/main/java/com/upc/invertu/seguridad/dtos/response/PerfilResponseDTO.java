package com.upc.invertu.seguridad.dtos.response;

import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** END-PROF-01, END-PROF-02 */
@Getter
@Setter
@NoArgsConstructor
public class PerfilResponseDTO {
    private String nombres;
    private String apellidos;
    private String correo;
    private String universidad;
    private Tema tema;
    private Idioma idioma;
    private String rol;
    private PlanDTO plan;

    /** Plan vigente del rol: precio y limites de uso */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlanDTO {
        private BigDecimal precioMensual;
        private Integer maxMetasActivas;
        private Integer maxRecordatorios;
    }
}
