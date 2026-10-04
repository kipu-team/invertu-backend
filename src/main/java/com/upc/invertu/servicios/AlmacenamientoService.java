package com.upc.invertu.servicios;

/** Guardado de comprobantes (local ahora, S3 en el despliegue) */
public interface AlmacenamientoService {
    String guardarTemporal(Long idEstudiante, byte[] contenido, String tipoArchivo);    // END-TRX-03
}
