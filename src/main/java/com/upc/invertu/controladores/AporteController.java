package com.upc.invertu.controladores;

import com.upc.invertu.dtos.response.AporteResponseDTO;
import com.upc.invertu.servicios.AporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** END-GOAL-06, 12, 13 */
@RestController
@RequestMapping("/api/v1/metas/{idMeta}/aportes")
public class AporteController {
    @Autowired
    private AporteService aporteService;

    /** END-GOAL-06: historial de aportes de una meta propia (Free y Premium) */
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<AporteResponseDTO>> listar(@PathVariable Long idMeta) {
        return ResponseEntity.ok(aporteService.listar(idMeta));
    }
}
