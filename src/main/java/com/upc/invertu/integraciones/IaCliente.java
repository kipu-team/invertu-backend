package com.upc.invertu.integraciones;

import org.springframework.stereotype.Component;

/**
 * Llamadas a la API externa de IA: chatbot (END-CHAT-01) y lectura de comprobantes (END-TRX-03).
 * La clave se lee de la variable de entorno IA_API_KEY (propiedad ia.api-key).
 * Si el servicio no responde, lanzar ServicioExternoException (503).
 */
@Component
public class IaCliente {
    // TODO: definir proveedor de IA e implementar las llamadas
}
