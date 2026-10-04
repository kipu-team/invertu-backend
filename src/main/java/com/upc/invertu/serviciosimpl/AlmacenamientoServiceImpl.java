package com.upc.invertu.serviciosimpl;

import com.upc.invertu.servicios.AlmacenamientoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    @Value("${almacenamiento.ruta}")
    private String ruta;

    /** END-TRX-03: guarda el comprobante en una carpeta temporal del estudiante. Devuelve la referencia (comprobanteRef) con la que END-TRX-02 lo confirmara (T-48).*/
    @Override
    public String guardarTemporal(Long idEstudiante, byte[] contenido, String tipoArchivo) {
        // Nombre aleatorio: no se puede adivinar y no usa el nombre original del archivo
        String referencia = UUID.randomUUID() + extension(tipoArchivo);
        Path carpeta = Path.of(ruta, "temporales", String.valueOf(idEstudiante));
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(referencia), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el comprobante temporal", e);
        }
        return referencia;
    }

    private String extension(String tipoArchivo) {
        return switch (tipoArchivo) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
