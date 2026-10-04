package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.AnalisisComprobanteResponseDTO;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.servicios.ComprobanteService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ComprobanteServiceImpl implements ComprobanteService {
    private static final long TAMANIO_MAXIMO = 5 * 1024 * 1024; // 5 MB en bytes
    private static final String ARCHIVO_NO_VALIDO = "Solo se aceptan archivos JPG, PNG o PDF de hasta 5 MB";

    /** END-TRX-03 (solo Premium, validado en el controller): analiza un comprobante con IA */
    @Override
    public AnalisisComprobanteResponseDTO analizar(MultipartFile archivo) {
        String tipoReal = validarArchivo(archivo); // se enviara a la IA en T-47

        // Pendiente T-48: limite de 10 comprobantes por hora (429)
        // Pendiente T-47: guardar el archivo temporal, enviarlo a la IA y devolver los datos detectados
        throw new ServicioExternoException("El servicio de IA aún no está disponible");
    }

    /**
     * Valida el tamano y el tipo REAL del archivo, leyendo sus primeros bytes (no la extension).
     * Devuelve el tipo detectado: image/jpeg, image/png o application/pdf.
     */
    private String validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > TAMANIO_MAXIMO) {
            throw new ReglaNegocioException(ARCHIVO_NO_VALIDO);
        }

        byte[] cabecera;
        try (InputStream entrada = archivo.getInputStream()) {
            cabecera = entrada.readNBytes(8);
        } catch (IOException e) {
            throw new ReglaNegocioException(ARCHIVO_NO_VALIDO);
        }

        if (empiezaCon(cabecera, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (empiezaCon(cabecera, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (empiezaCon(cabecera, 0x25, 0x50, 0x44, 0x46, 0x2D)) { // "%PDF-"
            return "application/pdf";
        }
        throw new ReglaNegocioException(ARCHIVO_NO_VALIDO);
    }

    private boolean empiezaCon(byte[] datos, int... firma) {
        if (datos.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
