package com.upc.invertu.servicios;

import com.upc.invertu.dtos.response.AnalisisComprobanteResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/** EP-04: analisis del comprobante con IA (END-TRX-03) */
public interface ComprobanteService {
    AnalisisComprobanteResponseDTO analizar(MultipartFile archivo);    // END-TRX-03
}
