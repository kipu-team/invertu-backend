package com.upc.invertu.programados;

import com.upc.invertu.configuracion.DataInitializer;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ServicioExternoException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.servicios.CorreoService;
import com.upc.invertu.utilidades.FormatoCorreo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**de  T-51 (US-32): es proceso diario, solo Premium. Evalua las suscripciones con alerta activa 3 dias antes del cobro; si el disponible del mes
 * (ingresos - gastos - aportes) no cubre el monto, envia la alerta (una sola vez por cobro).*/
@Slf4j
@Component
public class AlertaSaldoTarea {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final int DIAS_ANTES_DEL_COBRO = 3;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private AporteRepositorio aporteRepositorio;

    @Autowired
    private CorreoService correoService;

    /** Todos los dias a las 8:05 a. m. (hora de Lima), despues de que RecordatorioCobroTarea actualiza las fechas */
    @Scheduled(cron = "0 5 8 * * *", zone = "America/Lima")
    public void ejecutar() {
        procesar(LocalDate.now(ZONA_LIMA));
    }

    public void procesar(LocalDate hoy) {
        int enviadas = 0;
        // Cobros de hoy a 3 dias: hoy, manana, pasado y el tercer dia
        for (Suscripcion s : suscripcionRepositorio.conAlertaSaldoActiva(
                EstadoSuscripcion.ACTIVA, hoy, hoy.plusDays(DIAS_ANTES_DEL_COBRO))) {
            Estudiante estudiante = s.getEstudiante();

            if (!DataInitializer.ROLE_PREMIUM.equals(estudiante.getRol().getNombre())) {
                continue;
            }

            LocalDate desde = s.getProximaFechaCobro().minusDays(DIAS_ANTES_DEL_COBRO);
            if (s.getUltimaFechaAlertaSaldo() != null && !s.getUltimaFechaAlertaSaldo().isBefore(desde)) {
                continue;
            }

            BigDecimal disponible = disponibleDelMes(estudiante.getIdEstudiante(), YearMonth.from(hoy));
            if (disponible.compareTo(s.getMonto()) >= 0) {
                continue; // el disponible alcanza: no se envia nada
            }

            try {
                correoService.enviarAlertaSaldo(estudiante.getCorreo(), estudiante.getNombres(),
                        s.getNombreServicio(), FormatoCorreo.soles(s.getMonto()),
                        FormatoCorreo.fecha(s.getProximaFechaCobro()), FormatoCorreo.soles(disponible));
                s.setUltimaFechaAlertaSaldo(hoy);
                suscripcionRepositorio.save(s);
                enviadas++;
            } catch (ServicioExternoException e) {
                log.warn("No se pudo enviar la alerta de saldo de la suscripcion {}", s.getIdSuscripcion());
            }
        }
        log.info("Alertas de saldo {}: {} correos enviados", hoy, enviadas);
    }

    private BigDecimal disponibleDelMes(Long idEstudiante, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fin = mes.atEndOfMonth();

        BigDecimal disponible = BigDecimal.ZERO;
        for (Object[] fila : movimientoRepositorio.totalesDelMes(idEstudiante, inicio, fin)) {
            BigDecimal suma = (BigDecimal) fila[2];
            disponible = fila[0] == TipoMovimiento.INGRESO ? disponible.add(suma) : disponible.subtract(suma);
        }
        return disponible.subtract(aporteRepositorio.aportesDelMes(idEstudiante, inicio, fin));
    }
}