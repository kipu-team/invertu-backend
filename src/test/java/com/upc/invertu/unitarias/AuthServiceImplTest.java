package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.LimiteIntentosException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.seguridad.dtos.request.AuthRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RecuperarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.request.RestablecerContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.ValidarTokenRequestDTO;
import com.upc.invertu.seguridad.dtos.response.AuthResponseDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.dtos.response.ValidarTokenResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.seguridad.entidades.TokenRecuperacion;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.repositorios.TokenRecuperacionRepositorio;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import com.upc.invertu.seguridad.serviciosimpl.AuthServiceImpl;
import com.upc.invertu.seguridad.utilidades.HashUtil;
import com.upc.invertu.seguridad.utilidades.JwtUtil;
import com.upc.invertu.servicios.CorreoService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * US-01 a US-04: registro (END-AUTH-01), inicio de sesion (END-AUTH-02), recuperacion (END-AUTH-03)
 * y restablecimiento de contrasena (END-AUTH-04 y 05)
 */
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

    @Mock
    private TokenRecuperacionRepositorio tokenRecuperacionRepositorio;

    @Mock
    private CorreoService correoService;

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

    // ---------- US-03: solicitar enlace de recuperacion (END-AUTH-03) ----------

    private RecuperarContrasenaRequestDTO recuperar(String correo) {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200,http://otro.com");
        RecuperarContrasenaRequestDTO dto = new RecuperarContrasenaRequestDTO();
        dto.setCorreo(correo);
        return dto;
    }

    private Estudiante estudianteAna() {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        estudiante.setNombres("Ana");
        estudiante.setCorreo("ana@upc.edu.pe");
        return estudiante;
    }

    @Test
    void recuperacionCorreoRegistrado_invalidaAnterioresGuardaHashYEnviaEnlace() {
        when(estudianteRepositorio.findByCorreoConRol("ana@upc.edu.pe")).thenReturn(Optional.of(estudianteAna()));
        LocalDateTime antes = LocalDateTime.now();

        MensajeResponseDTO respuesta = authService.solicitarRecuperacion(recuperar(" Ana@UPC.edu.pe "));

        verify(limiteIntentosService).verificarBloqueo("recuperar:ana@upc.edu.pe", AuthServiceImpl.MENSAJE_ESPERA_RECUPERACION);
        verify(limiteIntentosService).registrarFallo("recuperar:ana@upc.edu.pe", 1, Duration.ofMinutes(1));
        verify(tokenRecuperacionRepositorio).invalidarTokensVigentes(1L);

        ArgumentCaptor<TokenRecuperacion> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRecuperacionRepositorio).save(tokenCaptor.capture());
        TokenRecuperacion guardado = tokenCaptor.getValue();

        ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarRecuperacion(eq("ana@upc.edu.pe"), eq("Ana"), enlaceCaptor.capture(), eq(30L));
        String enlace = enlaceCaptor.getValue();
        String prefijo = "http://localhost:4200/restablecer-contrasena?token=";
        assertTrue(enlace.startsWith(prefijo));
        String tokenOriginal = enlace.substring(prefijo.length());

        // En la BD solo el hash del token que viaja en el enlace
        assertNotEquals(tokenOriginal, guardado.getTokenHash());
        assertEquals(HashUtil.sha256(tokenOriginal), guardado.getTokenHash());
        assertEquals(1L, guardado.getEstudiante().getIdEstudiante());
        assertFalse(guardado.getUsado());
        assertFalse(guardado.getFechaExpiracion().isBefore(antes.plusMinutes(30)));
        assertTrue(guardado.getFechaExpiracion().isBefore(LocalDateTime.now().plusMinutes(31)));

        assertEquals("Si el correo está registrado, recibirás un enlace", respuesta.getMensaje());
    }

    @Test
    void recuperacionCorreoNoRegistrado_mismaRespuestaSinTokenNiCorreo() {
        when(estudianteRepositorio.findByCorreoConRol("nadie@upc.edu.pe")).thenReturn(Optional.empty());

        MensajeResponseDTO respuesta = authService.solicitarRecuperacion(recuperar("nadie@upc.edu.pe"));

        assertEquals("Si el correo está registrado, recibirás un enlace", respuesta.getMensaje());
        verify(limiteIntentosService).registrarFallo("recuperar:nadie@upc.edu.pe", 1, Duration.ofMinutes(1));
        verifyNoInteractions(tokenRecuperacionRepositorio, correoService);
    }

    @Test
    void recuperacionFalloDelCorreo_respondeIgual200ConTokenGuardado() {
        when(estudianteRepositorio.findByCorreoConRol("ana@upc.edu.pe")).thenReturn(Optional.of(estudianteAna()));
        doThrow(new ServicioExternoException("No se pudo enviar el correo"))
                .when(correoService).enviarRecuperacion(anyString(), anyString(), anyString(), anyLong());

        MensajeResponseDTO respuesta = authService.solicitarRecuperacion(recuperar("ana@upc.edu.pe"));

        assertEquals("Si el correo está registrado, recibirás un enlace", respuesta.getMensaje());
        verify(tokenRecuperacionRepositorio).save(any(TokenRecuperacion.class));
    }

    @Test
    void recuperacionAntesDeUnMinuto_lanza429SinConsultarNiEnviar() {
        doThrow(new LimiteIntentosException(AuthServiceImpl.MENSAJE_ESPERA_RECUPERACION))
                .when(limiteIntentosService)
                .verificarBloqueo("recuperar:ana@upc.edu.pe", AuthServiceImpl.MENSAJE_ESPERA_RECUPERACION);

        LimiteIntentosException ex = assertThrows(LimiteIntentosException.class,
                () -> authService.solicitarRecuperacion(recuperar("ana@upc.edu.pe")));

        assertEquals("Debes esperar 1 minuto para reenviar el enlace", ex.getMessage());
        verify(limiteIntentosService, never()).registrarFallo(anyString(), anyInt(), any());
        verifyNoInteractions(estudianteRepositorio, tokenRecuperacionRepositorio, correoService);
    }

    // ---------- US-04: restablecer contrasena (END-AUTH-04 y END-AUTH-05) ----------

    private static final String TOKEN = "token-del-enlace";

    private TokenRecuperacion tokenGuardado(boolean usado, LocalDateTime expiracion) {
        TokenRecuperacion token = new TokenRecuperacion();
        token.setEstudiante(estudianteAna());
        token.setTokenHash(HashUtil.sha256(TOKEN));
        token.setUsado(usado);
        token.setFechaExpiracion(expiracion);
        return token;
    }

    private ValidarTokenRequestDTO validar(String token) {
        ValidarTokenRequestDTO dto = new ValidarTokenRequestDTO();
        dto.setToken(token);
        return dto;
    }

    private RestablecerContrasenaRequestDTO restablecer(String token, String nueva, String confirmar) {
        RestablecerContrasenaRequestDTO dto = new RestablecerContrasenaRequestDTO();
        dto.setToken(token);
        dto.setNuevaContrasena(nueva);
        dto.setConfirmarContrasena(confirmar);
        return dto;
    }

    @Test
    void validarTokenVigente_buscaPorHashYRespondeValido() {
        when(tokenRecuperacionRepositorio.findByTokenHash(HashUtil.sha256(TOKEN)))
                .thenReturn(Optional.of(tokenGuardado(false, LocalDateTime.now().plusMinutes(10))));

        ValidarTokenResponseDTO respuesta = authService.validarToken(validar(TOKEN));

        assertTrue(respuesta.isValido());
        assertEquals("Enlace válido", respuesta.getMensaje());
        verify(tokenRecuperacionRepositorio, never()).findByTokenHash(TOKEN); // nunca busca el original
    }

    @Test
    void validarTokenInexistente_lanza400() {
        when(tokenRecuperacionRepositorio.findByTokenHash(anyString())).thenReturn(Optional.empty());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.validarToken(validar("otro-token")));

        assertEquals("El enlace expiró o no es válido", ex.getMessage());
    }

    @Test
    void validarTokenUsado_lanza400() {
        when(tokenRecuperacionRepositorio.findByTokenHash(HashUtil.sha256(TOKEN)))
                .thenReturn(Optional.of(tokenGuardado(true, LocalDateTime.now().plusMinutes(10))));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.validarToken(validar(TOKEN)));

        assertEquals("El enlace expiró o no es válido", ex.getMessage());
    }

    @Test
    void validarTokenVencido_lanza400() {
        when(tokenRecuperacionRepositorio.findByTokenHash(HashUtil.sha256(TOKEN)))
                .thenReturn(Optional.of(tokenGuardado(false, LocalDateTime.now().minusMinutes(1))));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.validarToken(validar(TOKEN)));

        assertEquals("El enlace expiró o no es válido", ex.getMessage());
    }

    @Test
    void restablecerExitoso_guardaContrasenaCifradaYMarcaTokenUsado() {
        TokenRecuperacion token = tokenGuardado(false, LocalDateTime.now().plusMinutes(10));
        when(tokenRecuperacionRepositorio.findByTokenHash(HashUtil.sha256(TOKEN))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Nueva123!")).thenReturn("hash-bcrypt-nuevo");

        MensajeResponseDTO respuesta = authService.restablecerContrasena(restablecer(TOKEN, "Nueva123!", "Nueva123!"));

        ArgumentCaptor<Estudiante> captor = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepositorio).save(captor.capture());
        assertEquals("hash-bcrypt-nuevo", captor.getValue().getPasswordHash());
        verify(tokenRecuperacionRepositorio).save(token);
        assertTrue(token.getUsado());
        assertEquals("Contraseña restablecida con éxito", respuesta.getMensaje());
    }

    @Test
    void restablecerContrasenasDistintas_lanza400SinTocarNada() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.restablecerContrasena(restablecer(TOKEN, "Nueva123!", "Otra123!")));

        assertEquals("Las contraseñas no coinciden", ex.getMessage());
        verifyNoInteractions(tokenRecuperacionRepositorio, estudianteRepositorio, passwordEncoder);
    }

    @Test
    void restablecerConTokenVencido_lanza400SinCambiarContrasena() {
        TokenRecuperacion token = tokenGuardado(false, LocalDateTime.now().minusMinutes(1));
        when(tokenRecuperacionRepositorio.findByTokenHash(HashUtil.sha256(TOKEN))).thenReturn(Optional.of(token));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.restablecerContrasena(restablecer(TOKEN, "Nueva123!", "Nueva123!")));

        assertEquals("El enlace expiró o no es válido", ex.getMessage());
        verifyNoInteractions(estudianteRepositorio, passwordEncoder);
        verify(tokenRecuperacionRepositorio, never()).save(any());
        assertFalse(token.getUsado());
    }
}
