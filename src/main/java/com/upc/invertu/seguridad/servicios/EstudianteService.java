package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.seguridad.dtos.request.PerfilRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PreferenciasRequestDTO;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.dtos.response.PreferenciasResponseDTO;

/** EP-08: perfil, preferencias, contrasena y pasar a Premium */
public interface EstudianteService {
    // TODO: declarar las operaciones del modulo

    PerfilResponseDTO consultarPerfil();            // END-PROF-01
    PerfilResponseDTO actualizarPerfil(PerfilRequestDTO dto); // END-PROF-02
    PreferenciasResponseDTO actualizarPreferencias(PreferenciasRequestDTO dto); // END-PROF-03
}
