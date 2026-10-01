package com.upc.invertu.unitarias;

import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.LimiteIntentosException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.seguridad.dtos.request.AuthRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.response.AuthResponseDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import com.upc.invertu.seguridad.serviciosimpl.AuthServiceImpl;
import com.upc.invertu.seguridad.utilidades.JwtUtil;
import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** US-01 y US-02: registro (END-AUTH-01) e inicio de sesion (END-AUTH-02) */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EstudianteRepositorio estudianteRepositorio;

    @Mock
    private RolRepositorio rolRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LimiteIntentosService limiteIntentosService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistroRequestDTO request(String correo, String contrasena, String confirmar) {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNombres("  Ana  ");
        dto.setApellidos(" Perez ");
        dto.setCorreo(correo);
        dto.setContrasena(contrasena);
        dto.setConfirmarContrasena(confirmar);
        return dto;
    }

    @Test
    void registroExitoso_creaEstudianteFreeConCorreoEnMinusculasYContrasenaCifrada() {
        when(estudianteRepositorio.existsByCorreo("ana@upc.edu.pe")).thenReturn(false);
        when(rolRepositorio.findByNombre("ROLE_FREE")).thenReturn(Optional.of(new Rol("ROLE_FREE")));
        when(passwordEncoder.encode("Clave123!")).thenReturn("hash-bcrypt");
        when(estudianteRepositorio.save(any(Estudiante.class))).thenAnswer(inv -> {
            Estudiante e = inv.getArgument(0);
            e.setIdEstudiante(1L);
            return e;
        });

        RegistroResponseDTO respuesta = authService.registrar(request(" Ana@UPC.edu.pe ", "Clave123!", "Clave123!"));

        ArgumentCaptor<Estudiante> captor = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepositorio).save(captor.capture());
        Estudiante guardado = captor.getValue();
        assertEquals("ana@upc.edu.pe", guardado.getCorreo());
        assertEquals("Ana", guardado.getNombres());
        assertEquals("Perez", guardado.getApellidos());
        assertEquals("hash-bcrypt", guardado.getPasswordHash());
        assertEquals("ROLE_FREE", guardado.getRol().getNombre());
        assertEquals(Tema.SISTEMA, guardado.getTema());
        assertEquals(Idioma.es_419, guardado.getIdioma());
        assertNull(guardado.getUniversidad());

        assertEquals(1L, respuesta.getIdEstudiante());
        assertEquals("Ana", respuesta.getNombres());
        assertEquals("ana@upc.edu.pe", respuesta.getCorreo());
        assertEquals("ROLE_FREE", respuesta.getRol());
    }

    @Test
    void correoYaRegistrado_lanza409() {
        when(estudianteRepositorio.existsByCorreo("ana@upc.edu.pe")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> authService.registrar(request("ANA@upc.edu.pe", "Clave123!", "Clave123!")));

        assertEquals("Este correo ya está registrado", ex.getMessage());
        verify(estudianteRepositorio, never()).save(any());
    }

    @Test
    void contrasenasDistintas_lanza400SinConsultarNiGuardar() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.registrar(request("ana@upc.edu.pe", "Clave123!", "Clave123?")));

        assertEquals("Las contraseñas no coinciden", ex.getMessage());
        verifyNoInteractions(estudianteRepositorio, rolRepositorio, passwordEncoder);
    }

    // ---------- US-02: inicio de sesion (END-AUTH-02) ----------

    private AuthRequestDTO login(String correo, String contrasena) {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setCorreo(correo);
        dto.setContrasena(contrasena);
        return dto;
    }

    @Test
    void loginExitoso_devuelveTokenYDatosYReiniciaElContador() {
        UserDetails usuario = User.withUsername("ana@upc.edu.pe").password("hash")
                .authorities("ROLE_FREE").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        when(jwtUtil.generarToken(usuario)).thenReturn("jwt-token");
        when(jwtUtil.getExpiracionSegundos()).thenReturn(86400L);

        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        estudiante.setNombres("Ana");
        estudiante.setCorreo("ana@upc.edu.pe");
        estudiante.setRol(new Rol("ROLE_FREE"));
        when(estudianteRepositorio.findByCorreoConRol("ana@upc.edu.pe")).thenReturn(Optional.of(estudiante));

        AuthResponseDTO respuesta = authService.iniciarSesion(login(" Ana@UPC.edu.pe ", "Clave123!"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("ana@upc.edu.pe", captor.getValue().getPrincipal());
        assertEquals("Clave123!", captor.getValue().getCredentials());

        verify(limiteIntentosService).verificarBloqueo("login:ana@upc.edu.pe", AuthServiceImpl.MENSAJE_BLOQUEO_LOGIN);
        verify(limiteIntentosService).reiniciarFallos("login:ana@upc.edu.pe");
        verify(limiteIntentosService, never()).registrarFallo(anyString(), anyInt(), any());

        assertEquals("jwt-token", respuesta.getToken());
        assertEquals("Bearer", respuesta.getTipo());
        assertEquals(86400L, respuesta.getExpiraEn());
        assertEquals(1L, respuesta.getEstudiante().getIdEstudiante());
        assertEquals("Ana", respuesta.getEstudiante().getNombres());
        assertEquals("ROLE_FREE", respuesta.getEstudiante().getRol());
        assertEquals(Tema.SISTEMA, respuesta.getEstudiante().getTema());
        assertEquals(Idioma.es_419, respuesta.getEstudiante().getIdioma());
    }

    @Test
    void credencialesIncorrectas_registraFalloYLanza401() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.iniciarSesion(login("ana@upc.edu.pe", "Incorrecta1!")));

        verify(limiteIntentosService).registrarFallo("login:ana@upc.edu.pe", 5, Duration.ofMinutes(15));
        verify(limiteIntentosService, never()).reiniciarFallos(anyString());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void correoBloqueado_lanza429SinAutenticar() {
        doThrow(new LimiteIntentosException(AuthServiceImpl.MENSAJE_BLOQUEO_LOGIN))
                .when(limiteIntentosService)
                .verificarBloqueo("login:ana@upc.edu.pe", AuthServiceImpl.MENSAJE_BLOQUEO_LOGIN);

        LimiteIntentosException ex = assertThrows(LimiteIntentosException.class,
                () -> authService.iniciarSesion(login("ANA@upc.edu.pe", "Clave123!")));

        assertEquals("Demasiados intentos fallidos. Intenta nuevamente en 15 minutos", ex.getMessage());
        verifyNoInteractions(authenticationManager, jwtUtil);
        verify(limiteIntentosService, never()).registrarFallo(anyString(), anyInt(), any());
    }
}
