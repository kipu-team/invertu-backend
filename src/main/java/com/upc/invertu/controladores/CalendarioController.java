package com.upc.invertu.controladores;

import com.upc.invertu.dtos.response.EventoCalendarioResponseDTO;
import com.upc.invertu.excepciones.ErrorResponseDTO;
import com.upc.invertu.servicios.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** END-CAL-01, 02 */
@RestController
@RequestMapping("/api/v1/calendario")
public class CalendarioController {
    public static final String MENSAJE_SOLO_PREMIUM = "El calendario financiero es exclusivo del plan Premium";

    @Autowired
    private CalendarioService calendarioService;

    /** END-CAL-01: eventos del mes (solo Premium). Free recibe 403 */
    @GetMapping
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<List<EventoCalendarioResponseDTO>> listarEventosDelMes(@RequestParam int anio, @RequestParam int mes){
        return ResponseEntity.ok(calendarioService.listarEventosDelMes(anio, mes));
    }
    /** 403 con el mensaje propio del calendario solo para este controller*/

    @ExceptionHandler (AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> accesoDenegato(AccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDTO(MENSAJE_SOLO_PREMIUM));
    }
}
