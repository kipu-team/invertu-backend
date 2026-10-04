package com.upc.invertu.unitarias;

import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.serviciosimpl.AlmacenamientoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** de US-17: guardado y confirmacion de comprobantes (END-TRX-03, END-TRX-02, END-TRX-06) */
class AlmacenamientoServiceImplTest {

    // Carpeta temporal que JUnit crea para cada prueba y borra al terminar
    @TempDir
    Path carpetaPrueba;

    private AlmacenamientoServiceImpl servicio;
    private final byte[] contenido = {1, 2, 3};

    @BeforeEach
    void configurar() {
        servicio = new AlmacenamientoServiceImpl();
        // Reemplaza el valor que normalmente pone @Value("${almacenamiento.ruta}")
        ReflectionTestUtils.setField(servicio, "ruta", carpetaPrueba.toString());
    }

    @Test
    void confirmarMueveElArchivoALaCarpetaDefinitiva() {
        String referencia = servicio.guardarTemporal(1L, contenido, "image/png");
        String ruta = servicio.confirmar(1L, referencia);
        assertEquals("definitivos/1/" + referencia, ruta);
        assertTrue(Files.exists(carpetaPrueba.resolve(ruta)));
        assertFalse(Files.exists(carpetaPrueba.resolve("temporales/1/" + referencia)));
    }

    @Test
    void unEstudianteNoPuedeConfirmarElComprobanteDeOtro() {
        String referencia = servicio.guardarTemporal(1L, contenido, "image/jpeg");
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(2L, referencia));
    }

    @Test
    void rechazaReferenciasQueIntentanSalirDeSuCarpeta() {
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(1L, "../2/archivo.jpg"));
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(1L, "inventado.exe"));
    }

    @Test
    void unComprobanteSoloSePuedeConfirmarUnaVez() {
        String referencia = servicio.guardarTemporal(1L, contenido, "application/pdf");
        servicio.confirmar(1L, referencia);
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(1L, referencia));
    }

    @Test
    void eliminarBorraElArchivo() {
        String ruta = servicio.confirmar(1L, servicio.guardarTemporal(1L, contenido, "image/png"));
        servicio.eliminar(ruta);
        assertFalse(Files.exists(carpetaPrueba.resolve(ruta)));
    }
}