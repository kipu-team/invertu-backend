package com.upc.invertu.seguridad.controladores;

import com.upc.invertu.seguridad.dtos.response.PasarPremiumResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.seguridad.dtos.request.CambiarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PerfilRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PreferenciasRequestDTO;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.dtos.response.PreferenciasResponseDTO;
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

    /** END-PROF-03: actualiza tema e idioma del estudiante autenticado (Free y Premium) */
    @PutMapping("/preferencias")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<PreferenciasResponseDTO> actualizarPreferencias(@RequestBody PreferenciasRequestDTO dto) {
        return ResponseEntity.ok(estudianteService.actualizarPreferencias(dto));
    }

    /** END-PROF-04: cambia la contrasena del estudiante autenticado (Free y Premium) */
    @PutMapping("/contrasena")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MensajeResponseDTO> cambiarContrasena(@RequestBody CambiarContrasenaRequestDTO dto) {
        return ResponseEntity.ok(estudianteService.cambiarContrasena(dto));
    }

    /**
     * END-PROF-05: activa el plan Premium en version de prueba (pago simulado, sin cobro).
     * Free y Premium pueden llamarlo; si ya es Premium el service responde 400.
     */
    @PostMapping("/pasar-premium")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<PasarPremiumResponseDTO> pasarAPremium() {
        return ResponseEntity.ok(estudianteService.pasarAPremium());
    }
}
