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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** END-SUB-03 a 09: respuestas segun la seccion 2.3 del documento (US-27 a US-32) */
@SpringBootTest
@ActiveProfiles("test")
class SuscripcionesDocumentoIntegracionTest {

    private static final String URL = "/api/v1/suscripciones";
    private static final String ANA = "ana.documento@upc.edu.pe";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        movimientoRepositorio.deleteAll();
        suscripcionRepositorio.deleteAll();
        estudianteRepositorio.deleteAll();
        mockMvc.perform(post("/api/v1/autenticacion/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombres":"Ana","apellidos":"Test","correo":"%s","contrasena":"Clave123!","confirmarContrasena":"Clave123!"}
                """.formatted(ANA)));
    }

    @AfterEach
    void limpiar() {
        movimientoRepositorio.deleteAll();
        suscripcionRepositorio.deleteAll();
        estudianteRepositorio.deleteAll();
    }
    //  END-SUB-08 (US-31): recordatorio

    @Test
    void activarYDesactivarRecordatorio() throws Exception {
        Long id = crear("Netflix", null);

        como("FREE", put(URL + "/" + id + "/recordatorio").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activo\":true,\"diasAnticipacion\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSuscripcion").value(id))
                .andExpect(jsonPath("$.recordatorioActivo").value(true))
                .andExpect(jsonPath("$.diasAnticipacion").value(3));

        como("FREE", put(URL + "/" + id + "/recordatorio").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordatorioActivo").value(false))
                .andExpect(jsonPath("$.diasAnticipacion").value(nullValue()));
    }

    @Test
    void diasDeAnticipacionNoValidosResponde400() throws Exception {
        Long id = crear("Netflix", null);

        como("FREE", put(URL + "/" + id + "/recordatorio").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activo\":true,\"diasAnticipacion\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Días de anticipación no válidos"));
    }

    @Test
    void planFreeNoPuedeActivarUnCuartoRecordatorio() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Long id = crear("Servicio " + i, null);
            como("FREE", put(URL + "/" + id + "/recordatorio").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"activo\":true,\"diasAnticipacion\":1}"))
                    .andExpect(status().isOk());
        }
        Long cuarta = crear("Servicio 4", null);

        como("FREE", put(URL + "/" + cuarta + "/recordatorio").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activo\":true,\"diasAnticipacion\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Con el plan Free puedes activar recordatorios en hasta 3 suscripciones"));
    }

    //  END-SUB-09 (US-32): alerta de saldo, solo para Premium

    @Test
    void alertaDeSaldoConPlanFreeResponde403() throws Exception {
        Long id = crear("Netflix", null);

        como("FREE", put(URL + "/" + id + "/alerta-saldo").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activa\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void alertaDeSaldoConPlanPremium() throws Exception {
        Long id = crear("Netflix", null);

        como("PREMIUM", put(URL + "/" + id + "/alerta-saldo").contentType(MediaType.APPLICATION_JSON)
                .content("{\"activa\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSuscripcion").value(id))
                .andExpect(jsonPath("$.alertaSaldoActiva").value(true));
    }

    //  END-SUB-07 (US-30): reactivar con la fecha en el cuerpo

    @Test
    void reactivarConLaFechaEnElCuerpo() throws Exception {
        Long id = crear("Netflix", null);
        como("FREE", patch(URL + "/" + id + "/cancelar")).andExpect(status().isOk());
        LocalDate nuevaFecha = LocalDate.now().plusDays(10);

        como("FREE", patch(URL + "/" + id + "/reactivar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"proximaFechaCobro\":\"%s\"}".formatted(nuevaFecha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.proximaFechaCobro").value(nuevaFecha.toString()));
    }

    @Test
    void reactivarConFechaPasadaResponde400() throws Exception {
        Long id = crear("Netflix", null);
        como("FREE", patch(URL + "/" + id + "/cancelar")).andExpect(status().isOk());

        como("FREE", patch(URL + "/" + id + "/reactivar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"proximaFechaCobro\":\"%s\"}".formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La fecha de cobro no puede ser anterior a hoy"));
    }

    // END-SUB-03 y 04 (US-27 y US-28): campos del listado y del detalle

    @Test
    void detalleSinPagosNoTieneUltimaCategoria() throws Exception {
        Long id = crear("Netflix", null);

        como("FREE", get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordatorioActivo").value(false))
                .andExpect(jsonPath("$.alertaSaldoActiva").value(false))
                .andExpect(jsonPath("$.ultimaCategoria").value(nullValue()))
                .andExpect(jsonPath("$.pagos").isEmpty());
    }

    @Test
    void detalleConPagoMuestraUltimaCategoriaYMedioDePagoNull() throws Exception {
        Long id = crear("Netflix", null);
        // Pago sin medio de pago (es opcional en END-TRX-02)
        como("FREE", post("/api/v1/movimientos").contentType(MediaType.APPLICATION_JSON).content("""
                {"tipo":"GASTO","clasificacion":"FIJO","descripcion":"Netflix","fecha":"%s","monto":39.90,
                 "idCategoria":1,"idSuscripcion":%d}""".formatted(LocalDate.now(), id)))
                .andExpect(status().isCreated());

        como("FREE", get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimaCategoria.idCategoria").value(1))
                .andExpect(jsonPath("$.ultimaCategoria.nombre").isNotEmpty())
                .andExpect(jsonPath("$.pagos[0].monto").value(39.90))
                .andExpect(jsonPath("$.pagos[0].medioPago").value(nullValue()));
    }

    @Test
    void listadoIncluyeRecordatorioActivo() throws Exception {
        crear("Netflix", null);

        como("FREE", get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordatorioActivo").value(false));
    }

    // END-SUB-01 y 05 (US-26 y US-29): textos sin espacios de mas

    @Test
    void descripcionVaciaSeGuardaComoNullYElNombreSinEspacios() throws Exception {
        Long id = crear("Netflix", "   ");

        como("FREE", get(URL + "/" + id)).andExpect(jsonPath("$.descripcion").value(nullValue()));

        como("FREE", put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreServicio":"  Netflix Premium  ","monto":45.90,"frecuencia":"MENSUAL",
                 "proximaFechaCobro":"%s","descripcion":"  Plan familiar  "}""".formatted(LocalDate.now().plusMonths(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreServicio").value("Netflix Premium"))
                .andExpect(jsonPath("$.descripcion").value("Plan familiar"));
    }

    private Long crear(String nombre, String descripcion) throws Exception {
        String campoDescripcion = descripcion == null ? "" : ",\"descripcion\":\"" + descripcion + "\"";
        String respuesta = como("FREE", post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombreServicio":"%s","monto":39.90,"frecuencia":"MENSUAL","proximaFechaCobro":"%s"%s}
                        """.formatted(nombre, LocalDate.now().plusMonths(1), campoDescripcion)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.valueOf(respuesta.replaceAll(".*\"idSuscripcion\":(\\d+).*", "$1"));
    }

    private ResultActions como(String rol, MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion.with(user(ANA).roles(rol)));
    }
}