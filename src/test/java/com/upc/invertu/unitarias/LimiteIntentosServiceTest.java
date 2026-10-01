package com.upc.invertu.unitarias;

import com.upc.invertu.excepciones.LimiteIntentosException;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LimiteIntentosServiceTest {

    private final LimiteIntentosService servicio = new LimiteIntentosService();
    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    @Test
    void permiteHastaElMaximoYBloqueaElSiguiente() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarIntento("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS);
        }

        LimiteIntentosException ex = assertThrows(LimiteIntentosException.class,
                () -> servicio.registrarIntento("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS));
        assertEquals("Demasiados intentos. Intenta nuevamente en unos minutos", ex.getMessage());
        assertThrows(LimiteIntentosException.class,
                () -> servicio.verificar("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS));
    }

    @Test
    void lasClavesSonIndependientes() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarIntento("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS);
        }

        assertDoesNotThrow(() -> servicio.registrarIntento("login:luis@upc.edu.pe", 5, QUINCE_MINUTOS));
    }

    @Test
    void losIntentosVencidosNoCuentan() {
        // Ventana negativa: cualquier intento registrado ya quedo fuera de la ventana
        for (int i = 0; i < 5; i++) {
            servicio.registrarIntento("ia:1", 1, Duration.ZERO.minusNanos(1));
        }
        assertDoesNotThrow(() -> servicio.verificar("ia:1", 1, Duration.ZERO.minusNanos(1)));
    }

    @Test
    void reiniciarBorraLosIntentos() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarIntento("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS);
        }

        servicio.reiniciar("login:ana@upc.edu.pe");

        assertDoesNotThrow(() -> servicio.registrarIntento("login:ana@upc.edu.pe", 5, QUINCE_MINUTOS));
    }
}
