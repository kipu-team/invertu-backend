package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.request.ConsultaChatbotRequestDTO;
import com.upc.invertu.dtos.response.ConsultaChatbotResponseDTO;
import com.upc.invertu.entidades.Categoria;
import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.LimiteIntentosException;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.integraciones.IaCliente;
import com.upc.invertu.repositorios.ChatbotRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.serviciosimpl.ChatbotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** US-09: chatbot financiero (END-CHAT-01) */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceImplTest {

    @Mock
    private ChatbotRepositorio chatbotRepositorio;

    @Mock
    private IaCliente iaCliente;

    @Mock
    private LimiteIntentosService limiteIntentosService;

    @Mock
    private EstudianteAutenticado estudianteAutenticado;

    @InjectMocks
    private ChatbotServiceImpl chatbotService;

    private ConsultaChatbotRequestDTO dto;

    @BeforeEach
    void setUp() {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(7L);
        estudiante.setNombres("Ana");
        estudiante.setApellidos("Perez");
        estudiante.setCorreo("ana@test.com");
        when(estudianteAutenticado.obtener()).thenReturn(estudiante);

        dto = new ConsultaChatbotRequestDTO();
        dto.setPregunta("  ¿En qué gasté más este mes?  ");
    }

    @Test
    void consultar_conDatos_devuelveRespuestaDeLaIaSinDatosPersonales() {
        Categoria comida = new Categoria();
        comida.setNombre("Alimentación");
        Movimiento ingreso = movimiento(TipoMovimiento.INGRESO, comida, "500.00", "Mesada");
        Movimiento gasto = movimiento(TipoMovimiento.GASTO, comida, "120.50", "Almuerzos");

        Meta meta = new Meta();
        meta.setIdMeta(3L);
        meta.setNombre("Laptop");
        meta.setMontoObjetivo(new BigDecimal("2000.00"));
        meta.setFechaObjetivo(LocalDate.now().plusMonths(6));

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setNombreServicio("Spotify");
        suscripcion.setMonto(new BigDecimal("19.90"));
        suscripcion.setFrecuencia(FrecuenciaSuscripcion.MENSUAL);
        suscripcion.setProximaFechaCobro(LocalDate.now().plusDays(5));

        when(chatbotRepositorio.buscarMovimientos(eq(7L), any(), any())).thenReturn(List.of(ingreso, gasto));
        when(chatbotRepositorio.sumarAportes(eq(7L), any(), any())).thenReturn(new BigDecimal("100.00"));
        when(chatbotRepositorio.buscarMetas(7L)).thenReturn(List.of(meta));
        when(chatbotRepositorio.sumarAportesPorMeta(7L))
                .thenReturn(Collections.singletonList(new Object[]{3L, new BigDecimal("500.00")}));
        when(chatbotRepositorio.buscarSuscripcionesActivas(7L)).thenReturn(List.of(suscripcion));
        when(iaCliente.generarRespuesta(anyString(), anyString())).thenReturn("Gastaste más en Alimentación.");

        ConsultaChatbotResponseDTO respuesta = chatbotService.consultar(dto);

        assertEquals("Gastaste más en Alimentación.", respuesta.getRespuesta());
        verify(limiteIntentosService).registrarIntento("chatbot:7", 20, Duration.ofHours(1));

        ArgumentCaptor<String> instrucciones = ArgumentCaptor.forClass(String.class);
        verify(iaCliente).generarRespuesta(instrucciones.capture(), eq("¿En qué gasté más este mes?"));
        String enviado = instrucciones.getValue();
        assertTrue(enviado.contains("Total de ingresos: S/ 500.00"));
        assertTrue(enviado.contains("Total de gastos: S/ 120.50"));
        assertTrue(enviado.contains("Disponible (ingresos - gastos - aportes del mes): S/ 279.50"));
        assertTrue(enviado.contains("Laptop | objetivo S/ 2000.00 | aportado S/ 500.00 | avance 25.00%"));
        assertTrue(enviado.contains("Spotify | S/ 19.90 | frecuencia MENSUAL"));
        assertTrue(enviado.contains(ChatbotServiceImpl.MENSAJE_SIN_INFORMACION));
        // No se envian datos personales
        assertFalse(enviado.contains("Ana"));
        assertFalse(enviado.contains("Perez"));
        assertFalse(enviado.contains("ana@test.com"));
    }

    @Test
    void consultar_superaLimite_lanza429ConMensajeDelChatbot() {
        doThrow(new LimiteIntentosException(LimiteIntentosService.MENSAJE_LIMITE))
                .when(limiteIntentosService).registrarIntento(anyString(), anyInt(), any(Duration.class));

        LimiteIntentosException ex = assertThrows(LimiteIntentosException.class,
                () -> chatbotService.consultar(dto));

        assertEquals("Has alcanzado el límite de consultas. Intenta nuevamente más tarde", ex.getMessage());
        verifyNoInteractions(chatbotRepositorio, iaCliente);
    }

    @Test
    void consultar_iaFalla_lanza503() {
        Categoria comida = new Categoria();
        comida.setNombre("Alimentación");
        when(chatbotRepositorio.buscarMovimientos(anyLong(), any(), any()))
                .thenReturn(List.of(movimiento(TipoMovimiento.GASTO, comida, "30.00", "Menú")));
        when(chatbotRepositorio.sumarAportes(anyLong(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(chatbotRepositorio.buscarMetas(anyLong())).thenReturn(List.of());
        when(chatbotRepositorio.buscarSuscripcionesActivas(anyLong())).thenReturn(List.of());
        when(iaCliente.generarRespuesta(anyString(), anyString()))
                .thenThrow(new ServicioExternoException(IaCliente.MENSAJE_NO_DISPONIBLE));

        ServicioExternoException ex = assertThrows(ServicioExternoException.class,
                () -> chatbotService.consultar(dto));

        assertEquals(IaCliente.MENSAJE_NO_DISPONIBLE, ex.getMessage());
    }

    @Test
    void consultar_sinDatos_respondeSinInformacionSinLlamarALaIa() {
        when(chatbotRepositorio.buscarMovimientos(anyLong(), any(), any())).thenReturn(List.of());
        when(chatbotRepositorio.sumarAportes(anyLong(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(chatbotRepositorio.buscarMetas(anyLong())).thenReturn(List.of());
        when(chatbotRepositorio.buscarSuscripcionesActivas(anyLong())).thenReturn(List.of());

        ConsultaChatbotResponseDTO respuesta = chatbotService.consultar(dto);

        assertEquals(ChatbotServiceImpl.MENSAJE_SIN_INFORMACION, respuesta.getRespuesta());
        verifyNoInteractions(iaCliente);
    }

    private Movimiento movimiento(TipoMovimiento tipo, Categoria categoria, String monto, String descripcion) {
        Movimiento m = new Movimiento();
        m.setTipo(tipo);
        m.setCategoria(categoria);
        m.setMonto(new BigDecimal(monto));
        m.setDescripcion(descripcion);
        m.setFecha(LocalDate.now().withDayOfMonth(1));
        return m;
    }
}
