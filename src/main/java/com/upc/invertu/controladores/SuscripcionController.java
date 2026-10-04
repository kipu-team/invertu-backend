package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.SuscripcionRequestDTO;
import com.upc.invertu.dtos.response.SuscripcionDetalleResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResponseDTO;
import com.upc.invertu.dtos.response.SuscripcionResumenResponseDTO;
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


    /** END-SUB-03: Visualizar listado de suscripciones del estudiante, opcionalmente filtrado por estado */
    @Operation(
            summary = "Consultar el listado de suscripciones",
            description = "Obtiene las suscripciones del estudiante autenticado, clasificadas opcionalmente por su estado (ACTIVA o CANCELADA). "
                    + "Calcula dinámicamente si posee algún pago sin registrar. Requiere rol FREE o PREMIUM.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de suscripciones obtenido con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuscripcionResumenResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado de filtro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesión/JWT inválido o ausente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<SuscripcionResumenResponseDTO>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(suscripcionService.listar(estado));
    }

        /** END-SUB-04: Visualizar el detalle completo de una suscripción específica con su historial de pagos */
    @Operation(
            summary = "Consultar el detalle de una suscripción",
            description = "Devuelve la información detallada de una suscripción específica y su historial de pagos asociados (Movimientos). "
                    + "Si no existe o pertenece a otro estudiante, retorna 404. Requiere rol FREE o PREMIUM.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la suscripción obtenido con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuscripcionDetalleResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Sesión/JWT inválido o ausente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada o no pertenece al estudiante",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<SuscripcionDetalleResponseDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(suscripcionService.obtenerDetalle(id));
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    @Operation(summary = "Editar una suscripción", description = "END-SUB-05: actualiza los datos de una suscripción activa del estudiante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "La suscripción no está activa o los datos son inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    public ResponseEntity<SuscripcionResponseDTO> editarSuscripcion(
            @PathVariable Long id,
            @Valid @RequestBody SuscripcionRequestDTO request) {
        return ResponseEntity.ok(suscripcionService.editarSuscripcion(id, request));
    }
}

