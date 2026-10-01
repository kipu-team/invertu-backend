package com.upc.invertu.integracion;

import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-01 / END-AUTH-01: escenarios del registro contra la API completa (H2 + Spring Security) */
@SpringBootTest
@ActiveProfiles("test")
class RegistroIntegracionTest {

    private static final String URL = "/api/v1/autenticacion/registro";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        estudianteRepositorio.deleteAll();
    }

    private ResultActions registrar(String nombres, String apellidos, String correo,
                                    String contrasena, String confirmar) throws Exception {
        String json = """
                {"nombres": "%s", "apellidos": "%s", "correo": "%s",
                 "contrasena": "%s", "confirmarContrasena": "%s"}
                """.formatted(nombres, apellidos, correo, contrasena, confirmar);
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ---------- Escenario 1: registro exitoso ----------

    @Test
    @Transactional // para leer el rol (LAZY) del estudiante guardado
    void escenario1_registroExitoso_201ConPlanFree() throws Exception {
        registrar("Ana", "Perez", "Ana.Perez@UPC.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEstudiante").isNumber())
                .andExpect(jsonPath("$.nombres").value("Ana"))
                .andExpect(jsonPath("$.correo").value("ana.perez@upc.edu.pe"))
                .andExpect(jsonPath("$.rol").value("ROLE_FREE"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        Estudiante guardado = estudianteRepositorio.findAll().get(0);
        assertEquals("ROLE_FREE", guardado.getRol().getNombre());
        assertTrue(passwordEncoder.matches("Clave123!", guardado.getPasswordHash()));
        assertNotEquals("Clave123!", guardado.getPasswordHash());
    }

    // ---------- Escenario 2: correo ya registrado ----------

    @Test
    void escenario2_correoYaRegistrado_409() throws Exception {
        registrar("Ana", "Perez", "ana@upc.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isCreated());

        // Mismo correo con otras mayusculas: tambien es duplicado
        registrar("Otra", "Persona", "ANA@upc.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este correo ya está registrado"));

        assertEquals(1, estudianteRepositorio.count());
    }

    // ---------- Escenario 3: datos invalidos ----------

    @Test
    void escenario3_campoVacio_400() throws Exception {
        registrar("", "Perez", "ana@upc.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Este campo es obligatorio"));
        assertEquals(0, estudianteRepositorio.count());
    }

    @Test
    void escenario3_soloEspacios_400() throws Exception {
        registrar("Ana", "   ", "ana@upc.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Este campo es obligatorio"));
        assertEquals(0, estudianteRepositorio.count());
    }

    @Test
    void escenario3_campoFaltante_400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Este campo es obligatorio"));
    }

    @Test
    void escenario3_correoInvalido_400() throws Exception {
        registrar("Ana", "Perez", "ana-upc.edu.pe", "Clave123!", "Clave123!")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ingresa un correo válido"));
        registrar("Ana", "Perez", "ana@upc", "Clave123!", "Clave123!")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ingresa un correo válido"));
        assertEquals(0, estudianteRepositorio.count());
    }

    @Test
    void escenario3_contrasenaSinRequisitos_400() throws Exception {
        String mensaje = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, "
                + "un número y un carácter especial";
        // corta, sin mayuscula, sin minuscula, sin numero, sin caracter especial
        for (String debil : new String[]{"Cl1!", "clave123!", "CLAVE123!", "Clavesss!", "Clave1234"}) {
            registrar("Ana", "Perez", "ana@upc.edu.pe", debil, debil)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(mensaje));
        }
        assertEquals(0, estudianteRepositorio.count());
    }

    @Test
    void escenario3_confirmacionDistinta_400() throws Exception {
        registrar("Ana", "Perez", "ana@upc.edu.pe", "Clave123!", "Clave123?")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Las contraseñas no coinciden"));
        assertEquals(0, estudianteRepositorio.count());
    }

    @Test
    void jsonMalFormado_400DatosInvalidos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"nombres\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"));
    }
}
