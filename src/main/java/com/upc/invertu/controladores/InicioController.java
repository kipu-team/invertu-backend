package com.upc.invertu.controladores;

import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.servicios.InicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** END-DASH-01 a 03 */
@RestController
@RequestMapping("/api/v1/inicio")
public class InicioController {
    // TODO: endpoints END-DASH-02 y 03

    @Autowired
    private InicioService inicioService;

    /** END-DASH-01 (US-05): datos de la orientacion inicial del estudiante autenticado */
    @GetMapping("/orientacion")
    @PreAuthorize("hasAnyRole('FREE','PREMIUM')")
    public ResponseEntity<OrientacionResponseDTO> obtenerOrientacion() {
        return ResponseEntity.ok(inicioService.obtenerOrientacion());
    }
}
