package com.upc.invertu.seguridad.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-PROF-05 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasarPremiumResponseDTO {
    private String rol;      // ROLE_PREMIUM
    private String token;    // JWT renovado con el nuevo rol
    private String mensaje;  // "¡Ya eres Premium!"
}
