package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.excepciones.ErrorResponseDTO;
import com.upc.invertu.servicios.SuscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** END-SUB-01 a 09 */
@RestController
@RequestMapping("/api/v1/suscripciones")
public class SuscripcionController {

    @Autowired
    private SuscripcionService suscripcionService;

    /** END-SUB-01: registra una suscripcion en estado ACTIVA (Free y Premium). 400 si la validacion falla */
    @Operation(
            summary = "Registrar una suscripción",
            description = "Crea una suscripción del estudiante autenticado en estado ACTIVA, con el recordatorio "
                    + "y la alerta de saldo desactivados, y sin generar movimientos. Requiere rol FREE o PREMIUM.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos de la suscripción a registrar",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuscripcionRequestDTO.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Suscripción creada correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuscripcionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesión/JWT inválido o ausente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<SuscripcionResponseDTO> registrar(@Valid @RequestBody SuscripcionRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(suscripcionService.registrar(dto));
    }

    /** END-SUB-02: frecuencias disponibles del enum FrecuenciaSuscripcion (Free y Premium). No consulta la BD */
    @GetMapping("/frecuencias")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<String>> listarFrecuencias() {
        return ResponseEntity.ok(suscripcionService.listarFrecuencias());
    }

    @Operation(summary = "Cancelar una suscripción activa (END-SUB-06)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción cancelada correctamente"),
            @ApiResponse(responseCode = "400", description = "La suscripción no está activa"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SuscripcionResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(suscripcionService.cancelarSuscripcion(id));
    }

    @Operation(summary = "Reactivar una suscripción cancelada (END-SUB-07)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción reactivada correctamente"),
            @ApiResponse(responseCode = "400", description = "La suscripción no está cancelada o la fecha es anterior a hoy"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<SuscripcionResponseDTO> reactivar(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate proximaFechaCobro) {
        return ResponseEntity.ok(suscripcionService.reactivarSuscripcion(id, proximaFechaCobro));
    }
}
