package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.*;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.servicios.ComprobanteService;
import com.upc.invertu.servicios.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** END-TRX-01 a 06 */
@RestController
@RequestMapping("/api/v1/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private ComprobanteService comprobanteService;

    /** END-TRX-02: registra un ingreso o gasto (Free y Premium) */
    @PostMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoResponseDTO> registrar(@Valid @RequestBody MovimientoRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(dto));
    }

    /**
     * END-TRX-01: movimientos del mes con busqueda y filtros opcionales (Free y Premium).
     * Ej: ?anio=2026&mes=9&busqueda=almuerzo&tipo=GASTO
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoListaResponseDTO> listar(
            @RequestParam int anio,
            @RequestParam int mes,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Clasificacion clasificacion,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) MedioPago medioPago) {
        return ResponseEntity.ok(movimientoService.listarDelMes(
                anio, mes, busqueda, tipo, clasificacion, idCategoria, medioPago));
    }

    /** END-TRX-04: detalle de un movimiento propio. 404 si no existe o es de otro estudiante */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoDetalleResponseDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerDetalle(id));
    }

    /** END-TRX-05: edita un movimiento propio (Free y Premium). 404 si no existe o es de otro estudiante */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody MovimientoRequestDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }

    /** END-TRX-06: elimina un movimiento propio (Free y Premium). 404 si no existe o es de otro estudiante */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MensajeResponseDTO> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.eliminar(id));
    }
    /** END-TRX-03: analiza un comprobante (JPG, PNG o PDF de hasta 5 MB) con IA. Solo Premium: un estudiante Free recibe 403. No registra el movimiento */
    @PostMapping(value = "/analisis-comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<AnalisisComprobanteResponseDTO> analizarComprobante(
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(comprobanteService.analizar(archivo));
    }
}
