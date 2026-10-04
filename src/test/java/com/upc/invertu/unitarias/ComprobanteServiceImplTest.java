package com.upc.invertu.unitarias;

import com.upc.invertu.dtos.response.AnalisisComprobanteResponseDTO;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ArchivoNoProcesableException;
import com.upc.invertu.excepciones.LimiteIntentosException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.integraciones.ComprobanteIaCliente;
import com.upc.invertu.integraciones.ComprobanteIaCliente.DatosLeidos;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.servicios.LimiteIntentosService;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.AlmacenamientoService;
import com.upc.invertu.serviciosimpl.ComprobanteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** de US-17: analisis de comprobantes con IA (END-TRX-03). Claude se simula con un mock para no gastar credito */
@ExtendWith(MockitoExtension.class)
class ComprobanteServiceImplTest {

    @Mock
    private ComprobanteIaCliente comprobanteIaCliente;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @Mock
    private EstudianteAutenticado estudianteAutenticado;

    @Mock
    private LimiteIntentosService limiteIntentosService;

    @InjectMocks
    private ComprobanteServiceImpl comprobanteService;

    // Empieza con la "firma" de un JPG
    private final MockMultipartFile boleta = new MockMultipartFile(
            "archivo", "boleta.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3});

    @BeforeEach
    void configurar() {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        lenient().when(estudianteAutenticado.obtener()).thenReturn(estudiante);
    }

    @Test
    void comprobanteLegibleDevuelveLosDatosYLaReferencia() {
        when(comprobanteIaCliente.leerComprobante(any(), eq("image/jpeg"))).thenReturn(Optional.of(
                new DatosLeidos(new BigDecimal("25.5"), "2026-09-30", "Tambo", "GASTO", true)));
        when(almacenamientoService.guardarTemporal(eq(1L), any(), eq("image/jpeg"))).thenReturn("ref.jpg");
        AnalisisComprobanteResponseDTO respuesta = comprobanteService.analizar(boleta);
        assertEquals("ref.jpg", respuesta.getComprobanteRef());
        assertEquals(new BigDecimal("25.50"), respuesta.getDatos().getMonto());
        assertEquals(LocalDate.of(2026, 9, 30), respuesta.getDatos().getFecha());
        assertEquals(TipoMovimiento.GASTO, respuesta.getDatos().getTipoSugerido());
    }

    @Test
    void comprobanteIlegibleResponde422YNoSeGuarda() {
        when(comprobanteIaCliente.leerComprobante(any(), any())).thenReturn(Optional.of(
                new DatosLeidos(BigDecimal.ZERO, "", "", "GASTO", false)));
        assertThrows(ArchivoNoProcesableException.class, () -> comprobanteService.analizar(boleta));
        verify(almacenamientoService, never()).guardarTemporal(anyLong(), any(), any());
    }

    @Test
    void alSuperarElLimiteResponde429ConSuMensajeYNoLlamaALaIa() {
        doThrow(new LimiteIntentosException("Demasiados intentos. Intenta nuevamente en unos minutos"))
                .when(limiteIntentosService).registrarIntento(eq("comprobante:1"), eq(10), any());
        LimiteIntentosException ex = assertThrows(LimiteIntentosException.class,
                () -> comprobanteService.analizar(boleta));
        assertEquals("Alcanzaste el límite de comprobantes por hora", ex.getMessage());
        verifyNoInteractions(comprobanteIaCliente);
    }

    @Test
    void archivoQueNoEsImagenNiPdfResponde400SinGastarIntentos() {
        MockMultipartFile texto = new MockMultipartFile(
                "archivo", "boleta.jpg", "image/jpeg", "no soy una imagen".getBytes());
        assertThrows(ReglaNegocioException.class, () -> comprobanteService.analizar(texto));
        verifyNoInteractions(limiteIntentosService, comprobanteIaCliente);
    }
}