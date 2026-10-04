package com.upc.invertu.integracion;

import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-27, US-29 y US-30 */
@SpringBootTest
@ActiveProfiles("test")
class SuscripcionesIntegracionTest {

    private static final String URL = "/api/v1/suscripciones";
    private static final String ANA = "ana.suscripciones@upc.edu.pe";
    private static final String LUIS = "luis.suscripciones@upc.edu.pe";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        movimientoRepositorio.deleteAll();
        suscripcionRepositorio.deleteAll();
        estudianteRepositorio.deleteAll();
        registrarEstudiante("Ana", ANA);
        registrarEstudiante("Luis", LUIS);
    }

    @AfterEach
    void limpiar() {
        movimientoRepositorio.deleteAll();
        suscripcionRepositorio.deleteAll();
        estudianteRepositorio.deleteAll();
    }
    // Errores que antes respondian 500

    @Test
    void detalleInexistenteResponde404() throws Exception {
        comoAna(get(URL + "/99999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La suscripción no existe"));
    }

    @Test
    void detalleDeOtroEstudianteResponde404() throws Exception {
        Long deLuis = crear(LUIS, LocalDate.now().plusMonths(1));

        comoAna(get(URL + "/" + deLuis)).andExpect(status().isNotFound());
    }

    @Test
    void cancelarDosVecesResponde400() throws Exception {
        Long id = crear(ANA, LocalDate.now().plusMonths(1));
        comoAna(patch(URL + "/" + id + "/cancelar")).andExpect(status().isOk());

        comoAna(patch(URL + "/" + id + "/cancelar")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solo puedes cancelar suscripciones activas"));
    }

    @Test
    void reactivarUnaActivaResponde400() throws Exception {
        Long id = crear(ANA, LocalDate.now().plusMonths(1));

        comoAna(patch(URL + "/" + id + "/reactivar").param("proximaFechaCobro", LocalDate.now().plusDays(5).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estadoDeFiltroInvalidoResponde400() throws Exception {
        comoAna(get(URL).param("estado", "XYZ")).andExpect(status().isBadRequest());
    }

    // Pago sin registrar antes el listado y el detalle respondian 500 ----------

    @Test
    void suscripcionRecienCreadaNoTienePagoSinRegistrar() throws Exception {
        // Cobro hoy: el cobro anterior (hace un mes) es de antes de crear la suscripcion
        Long id = crear(ANA, LocalDate.now());
        comoAna(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pagoSinRegistrar").value(false));
        comoAna(get(URL + "/" + id)).andExpect(status().isOk());
    }

    @Test
    void cobroVencidoSinPagoSeMarcaYAlPagarSeQuita() throws Exception {
        Long id = crear(ANA, LocalDate.now());
        // Simula que la suscripcion se creo hace 2 meses: el cobro de hace un mes ya se debia pagar
        jdbcTemplate.update("UPDATE suscripcion SET fecha_creacion = ? WHERE id_suscripcion = ?",
                LocalDate.now().minusMonths(2).atStartOfDay(), id);

        comoAna(get(URL)).andExpect(jsonPath("$[0].pagoSinRegistrar").value(true));

        // Registrar el pago (END-TRX-02) quita la marca
        comoAna(post("/api/v1/movimientos").contentType(MediaType.APPLICATION_JSON).content("""
                {"tipo":"GASTO","clasificacion":"FIJO","descripcion":"Netflix","fecha":"%s","monto":39.90,
                 "idCategoria":1,"idSuscripcion":%d}""".formatted(LocalDate.now(), id)))
                .andExpect(status().isCreated());

        comoAna(get(URL)).andExpect(jsonPath("$[0].pagoSinRegistrar").value(false));
    }


    private void registrarEstudiante(String nombre, String correo) throws Exception {
        mockMvc.perform(post("/api/v1/autenticacion/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombres":"%s","apellidos":"Test","correo":"%s","contrasena":"Clave123!","confirmarContrasena":"Clave123!"}
                """.formatted(nombre, correo)));
    }

    private Long crear(String correo, LocalDate proximoCobro) throws Exception {
        String respuesta = mockMvc.perform(post(URL).with(user(correo).roles("FREE"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombreServicio":"Netflix","monto":39.90,"frecuencia":"MENSUAL","proximaFechaCobro":"%s"}
                        """.formatted(proximoCobro))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.valueOf(respuesta.replaceAll(".*\"idSuscripcion\":(\\d+).*", "$1"));
    }

    private ResultActions comoAna(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(ANA).roles("FREE")));
    }
}