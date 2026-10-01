package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.MetaRequestDTO;
import com.upc.invertu.dtos.response.EstadoLimiteResponseDTO;
import com.upc.invertu.dtos.response.MetaResponseDTO;
import com.upc.invertu.dtos.response.MetaResumenResponseDTO;
import com.upc.invertu.servicios.MetaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** END-GOAL-01 a 05, 07 a 11, 14 */
@RestController
@RequestMapping("/api/v1/metas")
public class MetaController {
    @Autowired
    private MetaService metaService;

    /** END-GOAL-01: crea una meta (Free y Premium). 403 si alcanzo el limite de su plan */
    @PostMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<MetaResponseDTO> crear(@Valid @RequestBody MetaRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.crear(dto));
    }

    /** END-GOAL-02: indica si el estudiante puede crear mas metas activas (Free y Premium) */
    @GetMapping("/estado-limite")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<EstadoLimiteResponseDTO> consultarEstadoLimite() {
        return ResponseEntity.ok(metaService.consultarEstadoLimite());
    }

    /** END-GOAL-03: lista las metas activas con su progreso (Free y Premium) */
    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<MetaResumenResponseDTO>> listarActivas() {
        return ResponseEntity.ok(metaService.listarActivas());
    }

    /** END-GOAL-04: lista las metas cumplidas y canceladas (Free y Premium) */
    @GetMapping("/finalizadas")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<MetaResumenResponseDTO>> listarFinalizadas() {
        return ResponseEntity.ok(metaService.listarFinalizadas());
    }
}
