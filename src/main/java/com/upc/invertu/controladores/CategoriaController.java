package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.CategoriaRequestDTO;
import com.upc.invertu.dtos.response.CategoriaResponseDTO;
import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.servicios.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** END-CAT-01 a 03 */
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {
    @Autowired
    private CategoriaService categoriaService;

    /** END-CAT-01: lista las categorias disponibles (Free y Premium) */
    @GetMapping
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listarDisponibles());
    }

    /** END-CAT-02: crea una categoria personalizada. Un estudiante Free recibe 403 */
    @PostMapping
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO dto) {
        // @Valid aplica @NotBlank y @Size del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(dto));
    }

    /** END-CAT-03: elimina (desactiva) una categoria personalizada propia */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<MensajeResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.desactivar(id));
    }
}
