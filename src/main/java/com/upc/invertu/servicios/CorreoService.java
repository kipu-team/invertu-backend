package com.upc.invertu.servicios;

/** Envio de correos (recuperacion, recordatorio, alerta de saldo) */
public interface CorreoService {
    /** END-AUTH-03: envia el enlace para restablecer la contrasena (vence en minutosVigencia). */
    void enviarRecuperacion(String correo, String nombres, String enlace, long minutosVigencia);

    /** T-50 de US-31: recordatorio de un cobro proximo. Monto y fecha ya vienen con formato ("S/ 19.90", "05/10/2026"). */
    void enviarRecordatorioCobro(String correo, String nombres, String servicio, String monto, String fechaCobro);

    /** T-51 de US-32: el disponible del mes no alcanza para el proximo cobro. Montos y fecha ya vienen con formato. */
    void enviarAlertaSaldo(String correo, String nombres, String servicio, String monto, String fechaCobro,
                           String disponible);
}