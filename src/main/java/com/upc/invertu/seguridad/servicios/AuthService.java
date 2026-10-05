package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.seguridad.dtos.request.AuthRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RecuperarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RestablecerContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.ValidarTokenRequestDTO;
import com.upc.invertu.seguridad.dtos.response.AuthResponseDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.dtos.response.ValidarTokenResponseDTO;

/** EP-01: registro, inicio de sesion y recuperacion de contrasena */
public interface AuthService {
    RegistroResponseDTO registrar(RegistroRequestDTO dto);

    AuthResponseDTO iniciarSesion(AuthRequestDTO dto);

    MensajeResponseDTO solicitarRecuperacion(RecuperarContrasenaRequestDTO dto);

    ValidarTokenResponseDTO validarToken(ValidarTokenRequestDTO dto);

    MensajeResponseDTO restablecerContrasena(RestablecerContrasenaRequestDTO dto);
}
