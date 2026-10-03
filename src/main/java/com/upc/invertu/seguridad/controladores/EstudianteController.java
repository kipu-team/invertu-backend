package com.upc.invertu.seguridad.controladores;

import com.upc.invertu.seguridad.dtos.request.PerfilRequestDTO;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.servicios.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** END-PROF-01 a 05 */
@RestController
@RequestMapping("/api/v1/perfil")
public class EstudianteController {
    // TODO: endpoints del modulo

    @Autowired
    private EstudianteService estudianteService;

    /** END-PROF-01: perfil del estudiante autenticado con su rol y plan (Free y Premium) */
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<PerfilResponseDTO> consultarPerfil() {
        return ResponseEntity.ok(estudianteService.consultarPerfil());
    }

    /** END-PROF-02: actualiza nombres, apellidos y universidad del estudiante autenticado (Free y Premium) */
    @PutMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<PerfilResponseDTO> actualizarPerfil(@Valid @RequestBody PerfilRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.ok(estudianteService.actualizarPerfil(dto));
    }
}
