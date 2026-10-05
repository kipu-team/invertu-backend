package com.upc.invertu.seguridad.servicios;

import com.upc.invertu.excepciones.LimiteIntentosException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita intentos en login (5 fallidos -> 15 min), recuperacion y funciones con IA.
 * Al superar el limite se lanza LimiteIntentosException (429).
 * Contador en memoria con ventana deslizante: se pierde al reiniciar la aplicacion.
 */
@Service
public class LimiteIntentosService {

    public static final String MENSAJE_LIMITE = "Demasiados intentos. Intenta nuevamente en unos minutos";

    // clave (ej. "login:correo") -> momentos de los intentos dentro de la ventana
    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();

    /** Lanza 429 si la clave ya tiene maxIntentos dentro de la ventana; si no, registra el intento. */
    public void registrarIntento(String clave, int maxIntentos, Duration ventana) {
        verificar(clave, maxIntentos, ventana);
        // compute es atomico por clave: dos requests a la vez no pierden intentos
        intentos.compute(clave, (k, lista) -> {
            Deque<Instant> resultado = lista == null ? new ArrayDeque<>() : lista;
            resultado.addLast(Instant.now());
            return resultado;
        });
    }

    /** Lanza 429 si la clave ya tiene maxIntentos dentro de la ventana, sin registrar un intento nuevo. */
    public void verificar(String clave, int maxIntentos, Duration ventana) {
        int cantidad = contarVigentes(clave, ventana);
        if (cantidad >= maxIntentos) {
            throw new LimiteIntentosException(MENSAJE_LIMITE);
        }
    }

    /** Borra los intentos de la clave (ej. login exitoso). */
    public void reiniciar(String clave) {
        intentos.remove(clave);
    }

    // ---------- Fallos consecutivos con bloqueo fijo (login, END-AUTH-02) ----------

    // clave -> fallos consecutivos y, si se alcanzo el maximo, hasta cuando dura el bloqueo
    private record EstadoFallos(int consecutivos, Instant bloqueadoHasta) {}

    private final Map<String, EstadoFallos> fallos = new ConcurrentHashMap<>();

    /** Lanza 429 con el mensaje indicado si la clave esta bloqueada; si el bloqueo ya vencio, lo elimina. */
    public void verificarBloqueo(String clave, String mensaje) {
        Instant ahora = Instant.now();
        EstadoFallos estado = fallos.computeIfPresent(clave, (k, e) ->
                e.bloqueadoHasta() != null && !e.bloqueadoHasta().isAfter(ahora) ? null : e);
        if (estado != null && estado.bloqueadoHasta() != null) {
            throw new LimiteIntentosException(mensaje);
        }
    }

    /** Suma un fallo consecutivo; al llegar a maxFallos bloquea la clave durante "bloqueo" desde ahora. */
    public void registrarFallo(String clave, int maxFallos, Duration bloqueo) {
        fallos.compute(clave, (k, e) -> {
            int consecutivos = (e == null ? 0 : e.consecutivos()) + 1;
            Instant bloqueadoHasta = consecutivos >= maxFallos ? Instant.now().plus(bloqueo) : null;
            return new EstadoFallos(consecutivos, bloqueadoHasta);
        });
    }

    /** Borra los fallos consecutivos de la clave (login exitoso). */
    public void reiniciarFallos(String clave) {
        fallos.remove(clave);
    }

    // Quita los intentos vencidos y devuelve cuantos quedan dentro de la ventana
    private int contarVigentes(String clave, Duration ventana) {
        Instant limite = Instant.now().minus(ventana);
        Deque<Instant> lista = intentos.computeIfPresent(clave, (k, l) -> {
            while (!l.isEmpty() && l.peekFirst().isBefore(limite)) {
                l.pollFirst();
            }
            return l.isEmpty() ? null : l; // null elimina la clave del mapa
        });
        return lista == null ? 0 : lista.size();
    }
}
