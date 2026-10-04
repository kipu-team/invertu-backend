package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.AnalisisComprobanteResponseDTO;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ArchivoNoProcesableException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.integraciones.ComprobanteIaCliente;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.AlmacenamientoService;
import com.upc.invertu.servicios.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


@Service
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final long TAMANIO_MAXIMO = 5 * 1024 * 1024; // 5 MB en bytes
    private static final String ARCHIVO_NO_VALIDO = "Solo se aceptan archivos JPG, PNG o PDF de hasta 5 MB";
    private static final String NO_LEGIBLE = "No pudimos leer tu comprobante. Completa los datos manualmente";

    @Autowired
    private ComprobanteIaCliente comprobanteIaCliente;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-TRX-03 (solo Premium, validado en el controller): analiza un comprobante con IA */
    @Override
    public AnalisisComprobanteResponseDTO analizar(MultipartFile archivo) {
        String tipoReal = validarArchivo(archivo);
        byte[] contenido = leerContenido(archivo);
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        // Pendiente T-48: limite de 10 comprobantes por hora (429)

        // Si la IA no reconoce un comprobante legible, el estudiante lo completa a mano (422)
        ComprobanteIaCliente.DatosLeidos leidos = comprobanteIaCliente.leerComprobante(contenido, tipoReal)
                .filter(ComprobanteIaCliente.DatosLeidos::legible)
                .orElseThrow(() -> new ArchivoNoProcesableException(NO_LEGIBLE));

        AnalisisComprobanteResponseDTO.DatosComprobante datos = new AnalisisComprobanteResponseDTO.DatosComprobante();
        datos.setMonto(montoValido(leidos.monto()));
        datos.setFecha(fechaValida(leidos.fecha()));
        datos.setDescripcion(descripcionValida(leidos.descripcion()));
        datos.setTipoSugerido("INGRESO".equals(leidos.tipoSugerido()) ? TipoMovimiento.INGRESO : TipoMovimiento.GASTO);

        // Sin monto el comprobante no sirve para precargar el formulario
        if (datos.getMonto() == null) {
            throw new ArchivoNoProcesableException(NO_LEGIBLE);
        }

        // Solo se guarda cuando la lectura fue exitosa
        AnalisisComprobanteResponseDTO respuesta = new AnalisisComprobanteResponseDTO();
        respuesta.setComprobanteRef(almacenamientoService.guardarTemporal(idEstudiante, contenido, tipoReal));
        respuesta.setDatos(datos);
        return respuesta;
    }

    // ---------- Datos sugeridos por la IA: solo se aceptan si tienen sentido ----------

    // Monto mayor que 0, con 2 decimales; si no, null
    private BigDecimal montoValido(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    // Fecha YYYY-MM-DD que no sea futura; si no, null
    private LocalDate fechaValida(String fecha) {
        try {
            LocalDate valor = LocalDate.parse(fecha);
            return valor.isAfter(LocalDate.now()) ? null : valor;
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    // Texto sin espacios de mas y de maximo 250 caracteres (como en el registro manual); vacio -> null
    private String descripcionValida(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        String texto = descripcion.trim();
        return texto.length() > 250 ? texto.substring(0, 250) : texto;
    }

    // ---------- Validacion del archivo (T-46) ----------

    private byte[] leerContenido(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ReglaNegocioException(ARCHIVO_NO_VALIDO);
        }
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

    // Compara los primeros bytes del archivo con la "firma" de un formato
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
