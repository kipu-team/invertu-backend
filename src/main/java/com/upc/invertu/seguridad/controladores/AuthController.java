package com.upc.invertu.seguridad.controladores;

import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.seguridad.dtos.request.AuthRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RecuperarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RestablecerContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.ValidarTokenRequestDTO;
import com.upc.invertu.seguridad.dtos.response.AuthResponseDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.dtos.response.ValidarTokenResponseDTO;
import com.upc.invertu.seguridad.servicios.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** END-AUTH-01 a 05 (publicos) */
@RestController
@RequestMapping("/api/v1/autenticacion")
public class AuthController {
    @Autowired
    private AuthService authService;

    /** END-AUTH-01: registra una cuenta de estudiante con plan FREE */
    @PostMapping("/registro")
    public ResponseEntity<RegistroResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    /** END-AUTH-02: inicia sesion y devuelve el token JWT */
    @PostMapping("/iniciar-sesion")
    public ResponseEntity<AuthResponseDTO> iniciarSesion(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }

    /** END-AUTH-03: envia el enlace de recuperacion (misma respuesta aunque el correo no exista) */
    @PostMapping("/recuperar-contrasena")
    public ResponseEntity<MensajeResponseDTO> recuperarContrasena(@Valid @RequestBody RecuperarContrasenaRequestDTO dto) {
        return ResponseEntity.ok(authService.solicitarRecuperacion(dto));
    }

    /** END-AUTH-04: valida que el enlace este vigente y sin usar (el token va en el body) */
    @PostMapping("/restablecer-contrasena/validar")
    public ResponseEntity<ValidarTokenResponseDTO> validarToken(@Valid @RequestBody ValidarTokenRequestDTO dto) {
        return ResponseEntity.ok(authService.validarToken(dto));
    }

    /** END-AUTH-05: restablece la contrasena con el token del enlace */
    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<MensajeResponseDTO> restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequestDTO dto) {
        return ResponseEntity.ok(authService.restablecerContrasena(dto));
    }
}
