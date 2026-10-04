package com.upc.invertu.integracion;

import com.upc.invertu.configuracion.DataInitializer;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.servicios.CustomUserDetailsService;
import com.upc.invertu.seguridad.utilidades.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-40: cerrar sesion.
 * La API es stateless (JWT): el cierre de sesion lo hace la interfaz eliminando el token y el servidor
 * no guarda sesiones. Estas pruebas verifican el escenario 2: despues de cerrar sesion (sin token)
 * o con un token vencido o alterado, toda seccion protegida responde 401 "Sesión no válida".
 */
@SpringBootTest
@ActiveProfiles("test")
class CierreSesionIntegracionTest {

    // END-PROF-01, la seccion protegida del escenario 2 de la US-40
    private static final String URL_PROTEGIDA = "/api/v1/perfil";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        estudianteRepositorio.deleteAll();

        Estudiante estudiante = new Estudiante();
        estudiante.setNombres("Ana");
        estudiante.setApellidos("Perez");
        estudiante.setCorreo("ana@upc.edu.pe");
        estudiante.setPasswordHash(passwordEncoder.encode("Clave123!"));
        estudiante.setRol(rolRepositorio.findByNombre(DataInitializer.ROLE_FREE).orElseThrow());
        estudianteRepositorio.save(estudiante);
    }

    // Token firmado con la clave real pero con la vigencia indicada (negativa = ya vencido)
    private String token(long expiracionSegundos) {
        JwtUtil jwtUtil = new JwtUtil(secret, expiracionSegundos);
        return jwtUtil.generarToken(userDetailsService.loadUserByUsername("ana@upc.edu.pe"));
    }

    // ---------- Antes de cerrar sesion: con un token valido la seccion protegida responde ----------

    @Test
    void conSesionActiva_200() throws Exception {
        mockMvc.perform(get(URL_PROTEGIDA).header("Authorization", "Bearer " + token(3600)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@upc.edu.pe"));
    }

    // ---------- Escenario 2: acceso a una seccion protegida sin sesion ----------

    @Test
    void escenario2_sinToken_401() throws Exception {
        mockMvc.perform(get(URL_PROTEGIDA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sesión no válida"));
    }

    @Test
    void tokenVencido_401() throws Exception {
        mockMvc.perform(get(URL_PROTEGIDA).header("Authorization", "Bearer " + token(-60)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sesión no válida"));
    }

    @Test
    void tokenAlterado_401() throws Exception {
        String alterado = token(3600) + "x";
        mockMvc.perform(get(URL_PROTEGIDA).header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sesión no válida"));
    }

    @Test
    void tokenDeUnaCuentaQueYaNoExiste_401() throws Exception {
        String tokenValido = token(3600);
        estudianteRepositorio.deleteAll();

        mockMvc.perform(get(URL_PROTEGIDA).header("Authorization", "Bearer " + tokenValido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sesión no válida"));
    }

    @Test
    void headerSinBearer_401() throws Exception {
        mockMvc.perform(get(URL_PROTEGIDA).header("Authorization", token(3600)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sesión no válida"));
    }
}