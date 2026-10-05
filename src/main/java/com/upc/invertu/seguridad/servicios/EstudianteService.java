package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.seguridad.dtos.request.PerfilRequestDTO;
import com.upc.invertu.seguridad.dtos.request.CambiarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PreferenciasRequestDTO;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.dtos.response.PreferenciasResponseDTO;
import com.upc.invertu.seguridad.dtos.response.PasarPremiumResponseDTO;

/** EP-08: perfil, preferencias, contrasena y pasar a Premium */
public interface EstudianteService {

    PerfilResponseDTO consultarPerfil();            // END-PROF-01
    PerfilResponseDTO actualizarPerfil(PerfilRequestDTO dto); // END-PROF-02
    PreferenciasResponseDTO actualizarPreferencias(PreferenciasRequestDTO dto); // END-PROF-03
    MensajeResponseDTO cambiarContrasena(CambiarContrasenaRequestDTO dto); // END-PROF-04
    PasarPremiumResponseDTO pasarAPremium();         // END-PROF-05
}
