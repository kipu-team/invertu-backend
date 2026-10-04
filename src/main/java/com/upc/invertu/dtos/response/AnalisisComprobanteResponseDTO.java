package com.upc.invertu.dtos.response;

import com.upc.invertu.entidades.enums.TipoMovimiento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * END-TRX-03: datos que la IA detecto en el comprobante.
 * Solo sirven para precargar el formulario; END-TRX-02 los vuelve a validar al registrar.
 */

@Getter
@Setter
@NoArgsConstructor
public class AnalisisComprobanteResponseDTO {

    private String comprobanteRef;
    private DatosComprobante datos;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DatosComprobante {
        private BigDecimal monto;
        private LocalDate fecha;
        private String descripcion;
        private TipoMovimiento tipoSugerido;
    }
}
