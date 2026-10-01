package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.dtos.response.MovimientoDetalleResponseDTO;
import com.upc.invertu.dtos.response.MovimientoListaResponseDTO;
import com.upc.invertu.dtos.response.MovimientoResponseDTO;
import com.upc.invertu.servicios.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** END-TRX-01 a 06 */
@RestController
@RequestMapping("/api/v1/movimientos")
public class MovimientoController {
    @Autowired
    private MovimientoService movimientoService;

    /** END-TRX-02: registra un ingreso o gasto (Free y Premium) */
    @PostMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoResponseDTO> registrar(@Valid @RequestBody MovimientoRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(dto));
    }
    /** END-TRX-01: movimientos del mes (Free y Premium). Ej: ?anio=2026&mes=9 */
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MovimientoListaResponseDTO> listar(@RequestParam int anio, @RequestParam int mes) {
        return ResponseEntity.ok(movimientoService.listarDelMes(anio, mes));
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
}
