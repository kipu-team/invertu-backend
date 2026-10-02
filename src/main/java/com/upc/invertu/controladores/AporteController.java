package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.AporteRequestDTO;
import com.upc.invertu.dtos.response.AporteResponseDTO;
import com.upc.invertu.servicios.AporteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /** END-GOAL-12: registra un aporte; si se alcanza el objetivo, la meta pasa a CUMPLIDA */
    @PostMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<AporteResponseDTO> registrar(@PathVariable Long idMeta,
                                                       @Valid @RequestBody AporteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aporteService.registrar(idMeta, dto));
    }

    /** END-GOAL-13: elimina un aporte de una meta activa */
    @DeleteMapping("/{idAporte}")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<AporteResponseDTO> eliminar(@PathVariable Long idMeta,
                                                      @PathVariable Long idAporte) {
        return ResponseEntity.ok(aporteService.eliminar(idMeta, idAporte));
    }
}