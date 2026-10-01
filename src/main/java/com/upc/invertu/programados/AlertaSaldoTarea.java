package com.upc.invertu.programados;

import org.springframework.stereotype.Component;

/**
 * T-51 (US-32): proceso diario, solo Premium.
 * Evalua las suscripciones con alerta activa 3 dias antes del cobro; si el disponible del mes
 * (ingresos - gastos - aportes) no cubre el monto, envia la alerta (una sola vez por cobro).
 */
@Component
public class AlertaSaldoTarea {

    // @Scheduled(cron = "0 0 8 * * *", zone = "America/Lima")
    public void ejecutar() {
        // TODO
    }
}
