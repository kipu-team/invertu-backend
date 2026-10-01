package com.upc.invertu.seguridad.servicios;

import org.springframework.stereotype.Service;

/**
 * Limita intentos en login (5 fallidos -> 15 min), registro, recuperacion y funciones con IA.
 * Al superar el limite se lanza LimiteIntentosException (429).
 */
@Service
public class LimiteIntentosService {
    // TODO: contador en memoria por correo/IP
}
