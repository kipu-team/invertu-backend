package com.upc.invertu.servicios;

/** Envio de correos (recuperacion, recordatorio, alerta de saldo) */
public interface CorreoService {
    /** END-AUTH-03: envia el enlace para restablecer la contrasena (vence en minutosVigencia). */
    void enviarRecuperacion(String correo, String nombres, String enlace, long minutosVigencia);
}
