package com.upc.invertu.serviciosimpl;

import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.servicios.AlmacenamientoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private static final String COMPROBANTE_NO_VALIDO = "El comprobante no es válido o ya expiró";

    // Solo acepta referencias como las que genera guardarTemporal: UUID y extension permitida asi nadie puede enviar otras rutas
    private static final Pattern REFERENCIA_VALIDA = Pattern.compile("[0-9a-f-]{36}\\.(jpg|png|pdf)");

    @Value("${almacenamiento.ruta}")
    private String ruta;

    /**END-TRX-03: guarda el comprobante en una carpeta temporal del estudiante devuelve la referencia (comprobanteRef) con la que END-TRX-02 lo confirmaran*/
    @Override
    public String guardarTemporal(Long idEstudiante, byte[] contenido, String tipoArchivo) {
        // Nombre aleatorio: no se puede adivinar y no usa el nombre original del archivo
        String referencia = UUID.randomUUID() + extension(tipoArchivo);
        Path carpeta = carpeta("temporales", idEstudiante);
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(referencia), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el comprobante temporal", e);
        }
        return referencia;
    }

    /**END-TRX-02: mueve el comprobante de la carpeta temporal a la definitiva del estudiante.
     * Solo lo encuentra en la carpeta temporal del MISMO estudiante: un estudiante no puede usar el de otro devuelve la ruta que se guarda en movimiento.urlComprobante.*/
    @Override
    public String confirmar(Long idEstudiante, String comprobanteRef) {
        if (comprobanteRef == null || !REFERENCIA_VALIDA.matcher(comprobanteRef).matches()) {
            throw new ReglaNegocioException(COMPROBANTE_NO_VALIDO);
        }
        Path origen = carpeta("temporales", idEstudiante).resolve(comprobanteRef);
        if (!Files.exists(origen)) {
            throw new ReglaNegocioException(COMPROBANTE_NO_VALIDO);
        }
        Path destino = carpeta("definitivos", idEstudiante);
        try {
            Files.createDirectories(destino);
            Files.move(origen, destino.resolve(comprobanteRef));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo confirmar el comprobante", e);
        }
        return "definitivos/" + idEstudiante + "/" + comprobanteRef;
    }

    /** END-TRX-06: borra el archivo de un comprobante. Si falla, solo se registra: no impide eliminar el movimiento. */
    @Override
    public void eliminar(String rutaComprobante) {
        try {
            Files.deleteIfExists(Path.of(ruta, rutaComprobante));
        } catch (IOException e) {
            log.warn("No se pudo borrar un comprobante: {}", e.getClass().getSimpleName());
        }
    }

    private Path carpeta(String tipo, Long idEstudiante) {
        return Path.of(ruta, tipo, String.valueOf(idEstudiante));
    }

    private String extension(String tipoArchivo) {
        return switch (tipoArchivo) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
