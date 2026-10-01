package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.servicios.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @PostMapping
    public ResponseEntity<SuscripcionResponseDTO> crearSuscripcion(@Valid @RequestBody SuscripcionRequestDTO request) {
        return new ResponseEntity<>(suscripcionService.crearSuscripcion(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionResponseDTO> obtenerSuscripcion(@PathVariable Long id) {
        return ResponseEntity.ok(suscripcionService.obtenerSuscripcionPorId(id));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<SuscripcionResponseDTO>> listarSuscripcionesPorEstudiante(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(suscripcionService.obtenerSuscripcionesPorEstudiante(estudianteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuscripcionResponseDTO> actualizarSuscripcion(
            @PathVariable Long id,
            @Valid @RequestBody SuscripcionRequestDTO request) {
        return ResponseEntity.ok(suscripcionService.actualizarSuscripcion(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarSuscripcion(@PathVariable Long id) {
        suscripcionService.cancelarSuscripcion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivarSuscripcion(@PathVariable Long id) {
        suscripcionService.reactivarSuscripcion(id);
        return ResponseEntity.ok().build();
    }
}