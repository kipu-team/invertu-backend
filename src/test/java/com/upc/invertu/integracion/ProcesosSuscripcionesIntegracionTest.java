package com.upc.invertu.integracion;

import com.upc.invertu.configuracion.DataInitializer;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.programados.AlertaSaldoTarea;
import com.upc.invertu.programados.RecordatorioCobroTarea;
import com.upc.invertu.repositorios.CategoriaRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.servicios.CorreoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProcesosSuscripcionesIntegracionTest {

    // Escenarios del documento: cobro el 05/10/2026, el proceso corre el 02/10/2026 (3 dias antes)
    private static final LocalDate HOY = LocalDate.of(2026, 10, 2);
    private static final LocalDate COBRO = LocalDate.of(2026, 10, 5);

    @Autowired
    private RecordatorioCobroTarea recordatorioCobroTarea;

    @Autowired
    private AlertaSaldoTarea alertaSaldoTarea;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    // Reemplaza el envio real de correos: solo registra a quien se le habria enviado
    @MockitoBean
    private CorreoService correoService;

    @BeforeEach
    void configurar() {
        limpiar();
    }

    // Deja la base de prueba limpia: otras pruebas borran estudiantes y fallarian si quedan suscripciones
    @AfterEach
    void limpiar() {
        movimientoRepositorio.deleteAll();
        suscripcionRepositorio.deleteAll();
        estudianteRepositorio.deleteAll();
    }

    // T-50 (US-31): recordatorio

    @Test
    void envia3DiasAntesConElTextoDelDocumentoYUnaSolaVez() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        Suscripcion spotify = suscripcion(ana, "Spotify Premium", "19.90", COBRO);
        spotify.setRecordatorioActivo(true);
        spotify.setDiasAnticipacion(3);
        suscripcionRepositorio.save(spotify);

        recordatorioCobroTarea.procesar(HOY);
        recordatorioCobroTarea.procesar(HOY); // segunda ejecucion el mismo dia: no se repite

        verify(correoService, times(1)).enviarRecordatorioCobro(
                "ana@upc.edu.pe", "Ana", "Spotify Premium", "S/ 19.90", "05/10/2026");
        assertEquals(HOY, recargar(spotify).getUltimaFechaRecordatorio());
    }

    @Test
    void noEnviaAntesDeLosDiasDeAnticipacion() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        Suscripcion spotify = suscripcion(ana, "Spotify Premium", "19.90", COBRO);
        spotify.setRecordatorioActivo(true);
        spotify.setDiasAnticipacion(1); // recien el 04/10
        suscripcionRepositorio.save(spotify);

        recordatorioCobroTarea.procesar(HOY);

        verify(correoService, never()).enviarRecordatorioCobro(any(), any(), any(), any(), any());
    }

    @Test
    void noEnviaSiElRecordatorioEstaDesactivadoOLaSuscripcionCancelada() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        suscripcion(ana, "Sin recordatorio", "10.00", COBRO);
        Suscripcion cancelada = suscripcion(ana, "Cancelada", "10.00", COBRO);
        cancelada.setRecordatorioActivo(true);
        cancelada.setDiasAnticipacion(3);
        cancelada.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcionRepositorio.save(cancelada);

        recordatorioCobroTarea.procesar(HOY);

        verify(correoService, never()).enviarRecordatorioCobro(any(), any(), any(), any(), any());
    }

    @Test
    void siElCorreoFallaNoSeMarcaComoEnviado() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        Suscripcion spotify = suscripcion(ana, "Spotify Premium", "19.90", COBRO);
        spotify.setRecordatorioActivo(true);
        spotify.setDiasAnticipacion(3);
        suscripcionRepositorio.save(spotify);
        doThrow(new ServicioExternoException("sin conexion"))
                .when(correoService).enviarRecordatorioCobro(any(), any(), any(), any(), any());

        recordatorioCobroTarea.procesar(HOY);

        assertNull(recargar(spotify).getUltimaFechaRecordatorio()); // se reintentara al dia siguiente
    }

    @Test
    void avanzaLaProximaFechaDeCobroVencida() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        Suscripcion netflix = suscripcion(ana, "Netflix", "39.90", LocalDate.of(2026, 9, 20));

        recordatorioCobroTarea.procesar(HOY);

        assertEquals(LocalDate.of(2026, 10, 20), recargar(netflix).getProximaFechaCobro());
    }

    //  T-51 (US-32): alerta de saldo

    @Test
    void premiumConDisponibleInsuficienteRecibeLaAlertaUnaSolaVez() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_PREMIUM);
        Suscripcion spotify = conAlerta(suscripcion(ana, "Spotify Premium", "19.90", COBRO));
        movimiento(ana, TipoMovimiento.INGRESO, "12.00"); // disponible del mes: S/ 12.00

        alertaSaldoTarea.procesar(HOY);
        alertaSaldoTarea.procesar(HOY);

        verify(correoService, times(1)).enviarAlertaSaldo(
                "ana@upc.edu.pe", "Ana", "Spotify Premium", "S/ 19.90", "05/10/2026", "S/ 12.00");
        assertEquals(HOY, recargar(spotify).getUltimaFechaAlertaSaldo());
    }

    @Test
    void premiumConDisponibleSuficienteNoRecibeNada() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_PREMIUM);
        conAlerta(suscripcion(ana, "Spotify Premium", "19.90", COBRO));
        movimiento(ana, TipoMovimiento.INGRESO, "100.00");
        movimiento(ana, TipoMovimiento.GASTO, "50.00"); // disponible: S/ 50.00

        alertaSaldoTarea.procesar(HOY);

        verify(correoService, never()).enviarAlertaSaldo(any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void estudianteFreeNoRecibeAlertaAunqueLaTengaActiva() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_FREE);
        conAlerta(suscripcion(ana, "Spotify Premium", "19.90", COBRO));

        alertaSaldoTarea.procesar(HOY);

        verify(correoService, never()).enviarAlertaSaldo(any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void noEvaluaCobrosAMasDe3Dias() {
        Estudiante ana = estudiante("ana@upc.edu.pe", DataInitializer.ROLE_PREMIUM);
        conAlerta(suscripcion(ana, "Spotify Premium", "19.90", LocalDate.of(2026, 10, 6)));

        alertaSaldoTarea.procesar(HOY);

        verify(correoService, never()).enviarAlertaSaldo(any(), any(), any(), any(), any(), anyString());
    }

    private Estudiante estudiante(String correo, String rol) {
        Estudiante e = new Estudiante();
        e.setNombres("Ana");
        e.setApellidos("Test");
        e.setCorreo(correo);
        e.setPasswordHash("no-se-usa-en-esta-prueba");
        e.setRol(rolRepositorio.findByNombre(rol).orElseThrow());
        return estudianteRepositorio.save(e);
    }

    private Suscripcion suscripcion(Estudiante estudiante, String nombre, String monto, LocalDate cobro) {
        Suscripcion s = new Suscripcion();
        s.setEstudiante(estudiante);
        s.setNombreServicio(nombre);
        s.setMonto(new BigDecimal(monto));
        s.setFrecuencia(FrecuenciaSuscripcion.MENSUAL);
        s.setProximaFechaCobro(cobro);
        return suscripcionRepositorio.save(s);
    }

    private Suscripcion conAlerta(Suscripcion s) {
        s.setAlertaSaldoActiva(true);
        return suscripcionRepositorio.save(s);
    }

    private void movimiento(Estudiante estudiante, TipoMovimiento tipo, String monto) {
        Movimiento m = new Movimiento();
        m.setEstudiante(estudiante);
        m.setCategoria(categoriaRepositorio.findAll().get(0));
        m.setTipo(tipo);
        m.setClasificacion(Clasificacion.VARIABLE);
        m.setDescripcion("Prueba");
        m.setFecha(HOY);
        m.setMonto(new BigDecimal(monto));
        movimientoRepositorio.save(m);
    }

    private Suscripcion recargar(Suscripcion s) {
        return suscripcionRepositorio.findById(s.getIdSuscripcion()).orElseThrow();
    }
}