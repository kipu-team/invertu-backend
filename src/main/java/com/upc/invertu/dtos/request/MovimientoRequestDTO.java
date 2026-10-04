package com.upc.invertu.dtos.request;

import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** END-TRX-02, END-TRX-05 */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoRequestDTO {
    // TODO: atributos segun la tabla de endpoints del informe (seccion 2.3)
    @NotNull(message = "Selecciona si es ingreso o gasto")
    private TipoMovimiento tipo;

    @NotNull(message = "Selecciona si es fijo o variable")
    private Clasificacion clasificacion;

    @NotBlank(message = "Ingresa una descripción")
    @Size(max = 250, message = "La descripción puede tener como máximo 250 caracteres")
    private String descripcion;

    @NotNull(message = "Ingresa una fecha")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    @NotNull(message = "Ingresa un monto")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a S/ 0.00")
    @Digits(integer = 10, fraction = 2, message = "El monto admite como máximo dos decimales")
    private BigDecimal monto;

    @NotNull(message = "Selecciona una categoría")
    private Long idCategoria;

    // Opcional
    private MedioPago medioPago;

    // Opcional solo para gastos (pago de una suscripcion)
    private Long idSuscripcion;

    // Opcional referencia que devolvio END-TRX-03 (registro con IA). Solo se usa al registrar, no al editar
    @Size(max = 50, message = "El comprobante no es válido o ya expiró")
    private String comprobanteRef;
}
