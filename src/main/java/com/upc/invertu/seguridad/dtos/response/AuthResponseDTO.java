package com.upc.invertu.seguridad.dtos.response;

import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-AUTH-02 */
@Getter
@Setter
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private long expiraEn; // segundos
    private EstudianteSesionDTO estudiante;

    public AuthResponseDTO(String token, long expiraEn, EstudianteSesionDTO estudiante) {
        this.token = token;
        this.expiraEn = expiraEn;
        this.estudiante = estudiante;
    }

    /** Datos basicos del estudiante que inicia sesion */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstudianteSesionDTO {
        private Long idEstudiante;
        private String nombres;
        private String rol;
        private Tema tema;
        private Idioma idioma;
    }
}
