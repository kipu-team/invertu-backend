package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositorio extends JpaRepository<Suscripcion, Long> {

    /** END-TRX-02 y 05: la suscripcion debe ser del estudiante y estar en el estado indicado (ACTIVA). */
    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudianteAndEstado(
            Long idSuscripcion, Long idEstudiante, EstadoSuscripcion estado);

    /** END-SUB-03: todas las suscripciones del estudiante, del cobro mas proximo al mas lejano. */
    List<Suscripcion> findByEstudianteIdEstudianteOrderByProximaFechaCobroAsc(Long idEstudiante);

    /** END-SUB-03 y END-CAL-01: suscripciones de un estado (ACTIVA), del cobro mas proximo al mas lejano. */
    List<Suscripcion> findByEstudianteIdEstudianteAndEstadoOrderByProximaFechaCobroAsc(Long idEstudiante, EstadoSuscripcion estado);

    /** END-SUB-04 a 09: solo encuentra la suscripcion si pertenece al estudiante. */
    Optional<Suscripcion> findByIdSuscripcionAndEstudianteIdEstudiante(Long idSuscripcion, Long idEstudiante);

    /** END-SUB-08: cuantas suscripciones del estudiante tienen el recordatorio activo. */
    long countByEstudianteIdEstudianteAndEstadoAndRecordatorioActivoTrue(Long idEstudiante, EstadoSuscripcion estado);

    /** T-50: suscripciones de un estado activo cuya proxima fecha de cobro ya paso, para avanzarla. */
    List<Suscripcion> findByEstadoAndProximaFechaCobroBefore(EstadoSuscripcion estado, LocalDate fecha);

    /** T-50 de US-31: suscripciones ACTIVAS con recordatorio activo, con su estudiante (para el correo). */
    @Query("SELECT s FROM Suscripcion s JOIN FETCH s.estudiante " +
            "WHERE s.estado = :estado AND s.recordatorioActivo = true")
    List<Suscripcion> conRecordatorioActivo(@Param("estado") EstadoSuscripcion estado);

    /**T-51 (US-32): suscripciones ACTIVAS con alerta de saldo activa que se cobran entre dos fechas, con su estudiante y su rol (la alerta es solo Premium).*/
    @Query("SELECT s FROM Suscripcion s JOIN FETCH s.estudiante e JOIN FETCH e.rol " +
            "WHERE s.estado = :estado AND s.alertaSaldoActiva = true " +
            "AND s.proximaFechaCobro BETWEEN :desde AND :hasta")
    List<Suscripcion> conAlertaSaldoActiva(@Param("estado") EstadoSuscripcion estado,
                                           @Param("desde") LocalDate desde,
                                           @Param("hasta") LocalDate hasta);
}