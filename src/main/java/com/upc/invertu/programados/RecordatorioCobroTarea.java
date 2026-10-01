package com.upc.invertu.programados;

import org.springframework.stereotype.Component;

/**
 * T-50 (US-31): proceso diario.
 * 1. Envia el recordatorio por correo de las suscripciones ACTIVAS con recordatorio activo,
 *    segun sus dias de anticipacion (una sola vez por cobro: ultima_fecha_recordatorio).
 * 2. Actualiza las proximas fechas de cobro vencidas segun la frecuencia.
 */
@Component
public class RecordatorioCobroTarea {

    // @Scheduled(cron = "0 0 8 * * *", zone = "America/Lima")
    public void ejecutar() {
        // TODO
    }
}
