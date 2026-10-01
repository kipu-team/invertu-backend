package com.upc.invertu.seguridad.serviciosimpl;

import com.upc.invertu.configuracion.DataInitializer;
import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.seguridad.dtos.request.AuthRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.response.AuthResponseDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.servicios.AuthService;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import com.upc.invertu.seguridad.utilidades.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LimiteIntentosService limiteIntentosService;

    public static final String MENSAJE_BLOQUEO_LOGIN = "Demasiados intentos fallidos. Intenta nuevamente en 15 minutos";
    private static final int MAX_FALLOS_LOGIN = 5;
    private static final Duration BLOQUEO_LOGIN = Duration.ofMinutes(15);

    /** END-AUTH-01: registra un estudiante con rol FREE (tema SISTEMA e idioma es_419 por defecto) */
    @Override
    @Transactional
    public RegistroResponseDTO registrar(RegistroRequestDTO dto) {
        if (!dto.getContrasena().equals(dto.getConfirmarContrasena())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }

        // El correo se guarda en minusculas: "Ana@UPC.edu.pe" y "ana@upc.edu.pe" son el mismo
        String correo = dto.getCorreo().trim().toLowerCase();
        if (estudianteRepositorio.existsByCorreo(correo)) {
            throw new ConflictoException("Este correo ya está registrado");
        }

        // DataInitializer crea el rol al iniciar; si faltara es un error de configuracion (500)
        Rol rolFree = rolRepositorio.findByNombre(DataInitializer.ROLE_FREE)
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + DataInitializer.ROLE_FREE));

        Estudiante estudiante = new Estudiante();
        estudiante.setNombres(dto.getNombres().trim());
        estudiante.setApellidos(dto.getApellidos().trim());
        estudiante.setCorreo(correo);
        estudiante.setPasswordHash(passwordEncoder.encode(dto.getContrasena())); // BCrypt
        estudiante.setRol(rolFree);
        estudiante = estudianteRepositorio.save(estudiante);

        return new RegistroResponseDTO(
                estudiante.getIdEstudiante(),
                estudiante.getNombres(),
                estudiante.getCorreo(),
                rolFree.getNombre());
    }

    /** END-AUTH-02: valida credenciales y devuelve el JWT; 5 fallos consecutivos bloquean 15 minutos */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO iniciarSesion(AuthRequestDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();
        String clave = "login:" + correo;

        // Si el correo esta bloqueado responde 429 sin revisar la contrasena
        limiteIntentosService.verificarBloqueo(clave, MENSAJE_BLOQUEO_LOGIN);

        Authentication autenticacion;
        try {
            // Usa CustomUserDetailsService y BCrypt; correo inexistente tambien lanza BadCredentialsException
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, dto.getContrasena()));
        } catch (BadCredentialsException ex) {
            limiteIntentosService.registrarFallo(clave, MAX_FALLOS_LOGIN, BLOQUEO_LOGIN);
            throw ex; // GlobalExceptionHandler -> 401 "Correo o contraseña incorrectos"
        }

        limiteIntentosService.reiniciarFallos(clave);

        String token = jwtUtil.generarToken((UserDetails) autenticacion.getPrincipal());
        Estudiante estudiante = estudianteRepositorio.findByCorreoConRol(correo)
                .orElseThrow(() -> new BadCredentialsException("Estudiante no encontrado"));

        return new AuthResponseDTO(token, jwtUtil.getExpiracionSegundos(),
                new AuthResponseDTO.EstudianteSesionDTO(
                        estudiante.getIdEstudiante(),
                        estudiante.getNombres(),
                        estudiante.getRol().getNombre(),
                        estudiante.getTema(),
                        estudiante.getIdioma()));
    }
}
