package com.upc.invertu.programados;

import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.servicios.CorreoService;
import com.upc.invertu.utilidades.FormatoCorreo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/** De T-50 se ctualiza las proximas fechas de cobro vencidas segun la frecuencia. Envia el recordatorio por correo de las suscripciones ACTIVAS con recordatorio activo,
 * segun sus dias de anticipacion (una sola vez por cobro: ultima_fecha_recordatorio).*/
@Slf4j
@Component
public class RecordatorioCobroTarea {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private CorreoService correoService;

    /** Todos los dias a las 8:00 a. m. (hora de Lima) */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Lima")
    public void ejecutar() {
        procesar(LocalDate.now(ZONA_LIMA));
    }

    /** Separado de ejecutar() para poder probarlo con cualquier fecha */
    public void procesar(LocalDate hoy) {
        int avanzadas = avanzarFechasVencidas(hoy);
        int enviados = enviarRecordatorios(hoy);
        log.info("Recordatorios {}: {} fechas de cobro actualizadas, {} correos enviados", hoy, avanzadas, enviados);
    }

    /**
     * Si la proxima fecha de cobro ya paso, se mueve al siguiente periodo (US-28: "avanza automaticamente
     * segun la frecuencia"). Asi el estudiante ve siempre el proximo cobro, y el cobro que paso queda como
     * "pago sin registrar" hasta que registre el pago.
     */
    private int avanzarFechasVencidas(LocalDate hoy) {
        int avanzadas = 0;
        for (Suscripcion s : suscripcionRepositorio.findByEstadoAndProximaFechaCobroBefore(EstadoSuscripcion.ACTIVA, hoy)) {
            LocalDate proxima = s.getProximaFechaCobro();
            while (proxima.isBefore(hoy)) {
                proxima = sumarPeriodo(proxima, s.getFrecuencia());
            }
            s.setProximaFechaCobro(proxima);
            suscripcionRepositorio.save(s);
            avanzadas++;
        }
        return avanzadas;
    }

    private int enviarRecordatorios(LocalDate hoy) {
        int enviados = 0;
        for (Suscripcion s : suscripcionRepositorio.conRecordatorioActivo(EstadoSuscripcion.ACTIVA)) {
            if (!tocaRecordar(s, hoy)) {
                continue;
            }
            Estudiante estudiante = s.getEstudiante();
            try {
                correoService.enviarRecordatorioCobro(estudiante.getCorreo(), estudiante.getNombres(),
                        s.getNombreServicio(), FormatoCorreo.soles(s.getMonto()),
                        FormatoCorreo.fecha(s.getProximaFechaCobro()));
                // Se guarda solo si el correo salio: si fallo, se reintenta al dia siguiente
                s.setUltimaFechaRecordatorio(hoy);
                suscripcionRepositorio.save(s);
                enviados++;
            } catch (ServicioExternoException e) {
                // Un correo que falla no detiene el resto; no se registra el contenido del correo
                log.warn("No se pudo enviar el recordatorio de la suscripcion {}", s.getIdSuscripcion());
            }
        }
        return enviados;
    }

    /**
     * Toca recordar si hoy esta entre "dias de anticipacion" antes del cobro y el dia del cobro,
     * y todavia no se envio un recordatorio para ESTE cobro.
     * La ventana (y no solo el dia exacto) permite enviarlo al dia siguiente si un dia el servidor estuvo apagado.
     */
    private boolean tocaRecordar(Suscripcion s, LocalDate hoy) {
        if (s.getDiasAnticipacion() == null || s.getProximaFechaCobro() == null) {
            return false;
        }
        LocalDate cobro = s.getProximaFechaCobro();
        LocalDate desde = cobro.minusDays(s.getDiasAnticipacion());
        boolean enVentana = !hoy.isBefore(desde) && !hoy.isAfter(cobro);

        // Una sola vez por cobro: un recordatorio enviado despues del cobro anterior ya es de este cobro
        LocalDate cobroAnterior = restarPeriodo(cobro, s.getFrecuencia());
        boolean yaEnviado = s.getUltimaFechaRecordatorio() != null
                && s.getUltimaFechaRecordatorio().isAfter(cobroAnterior);

        return enVentana && !yaEnviado;
    }

    private LocalDate sumarPeriodo(LocalDate fecha, FrecuenciaSuscripcion frecuencia) {
        return switch (frecuencia) {
            case SEMANAL    -> fecha.plusWeeks(1);
            case MENSUAL    -> fecha.plusMonths(1);
            case TRIMESTRAL -> fecha.plusMonths(3);
            case SEMESTRAL  -> fecha.plusMonths(6);
            case ANUAL      -> fecha.plusYears(1);
        };
    }

    private LocalDate restarPeriodo(LocalDate fecha, FrecuenciaSuscripcion frecuencia) {
        return switch (frecuencia) {
            case SEMANAL    -> fecha.minusWeeks(1);
            case MENSUAL    -> fecha.minusMonths(1);
            case TRIMESTRAL -> fecha.minusMonths(3);
            case SEMESTRAL  -> fecha.minusMonths(6);
            case ANUAL      -> fecha.minusYears(1);
        };
    }
}