package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * END-CHAT-01 (US-09): consultas de solo lectura con los datos del estudiante que recibe la IA.
 * No crea, edita ni elimina registros.
 */
public interface ChatbotRepositorio extends JpaRepository<Movimiento, Long> {

    /** Movimientos del periodo con su categoria (JOIN FETCH evita una consulta por movimiento). */
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.categoria " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "ORDER BY m.fecha, m.idMovimiento")
    List<Movimiento> buscarMovimientos(@Param("idEstudiante") Long idEstudiante,
                                       @Param("inicio") LocalDate inicio,
                                       @Param("fin") LocalDate fin);

    /** Suma de los aportes a metas del estudiante en el periodo (0 si no hay). */
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM Aporte a " +
            "WHERE a.meta.estudiante.idEstudiante = :idEstudiante " +
            "AND a.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumarAportes(@Param("idEstudiante") Long idEstudiante,
                            @Param("inicio") LocalDate inicio,
                            @Param("fin") LocalDate fin);

    /** Metas del estudiante en cualquier estado. */
    @Query("SELECT m FROM Meta m WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "ORDER BY m.fechaObjetivo, m.idMeta")
    List<Meta> buscarMetas(@Param("idEstudiante") Long idEstudiante);

    /** Total aportado por meta: cada fila es [idMeta, montoAportado]. */
    @Query("SELECT a.meta.idMeta, COALESCE(SUM(a.monto), 0) FROM Aporte a " +
            "WHERE a.meta.estudiante.idEstudiante = :idEstudiante " +
            "GROUP BY a.meta.idMeta")
    List<Object[]> sumarAportesPorMeta(@Param("idEstudiante") Long idEstudiante);

    /** Suscripciones activas ordenadas por proximo cobro. */
    @Query("SELECT s FROM Suscripcion s WHERE s.estudiante.idEstudiante = :idEstudiante " +
            "AND s.estado = com.upc.invertu.entidades.enums.EstadoSuscripcion.ACTIVA " +
            "ORDER BY s.proximaFechaCobro, s.idSuscripcion")
    List<Suscripcion> buscarSuscripcionesActivas(@Param("idEstudiante") Long idEstudiante);
}
