package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;

/** EP-01: registro, inicio de sesion y recuperacion de contrasena */
public interface AuthService {
    RegistroResponseDTO registrar(RegistroRequestDTO dto);
}
