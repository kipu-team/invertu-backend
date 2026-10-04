package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepositorio extends JpaRepository<Movimiento, Long> {

    /**
     * END-TRX-01 (US-10 y US-14): movimientos del mes con busqueda y filtros opcionales que se combinan.
     * Si un filtro llega null, esa condicion no filtra nada.
     * JOIN FETCH trae la categoria y la suscripcion en la misma consulta.
     */
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.categoria LEFT JOIN FETCH m.suscripcion " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "AND LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "AND (:tipo IS NULL OR m.tipo = :tipo) " +
            "AND (:clasificacion IS NULL OR m.clasificacion = :clasificacion) " +
            "AND (:idCategoria IS NULL OR m.categoria.idCategoria = :idCategoria) " +
            "AND (:medioPago IS NULL OR m.medioPago = :medioPago) " +
            "ORDER BY m.fecha DESC, m.idMovimiento DESC")
    List<Movimiento> buscar(@Param("idEstudiante") Long idEstudiante,
                            @Param("inicio") LocalDate inicio,
                            @Param("fin") LocalDate fin,
                            @Param("busqueda") String busqueda,
                            @Param("tipo") TipoMovimiento tipo,
                            @Param("clasificacion") Clasificacion clasificacion,
                            @Param("idCategoria") Long idCategoria,
                            @Param("medioPago") MedioPago medioPago);

    /** END-TRX-04, 05 y 06: solo encuentra el movimiento si pertenece al estudiante. */
    Optional<Movimiento> findByIdMovimientoAndEstudianteIdEstudiante(Long idMovimiento, Long idEstudiante);

    /** END-DASH-01 (US-05): indica si el estudiante tiene al menos un movimiento (ingreso o gasto). */
    boolean existsByEstudianteIdEstudiante(Long idEstudiante);

    /**
     * END-DASH-02 (US-07): totales del mes agrupados por tipo y clasificacion.
     * Cada fila es [tipo, clasificacion, suma]. Si no hay movimientos, la lista llega vacia.
     */
    @Query("SELECT m.tipo, m.clasificacion, COALESCE(SUM(m.monto), 0) FROM Movimiento m " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY m.tipo, m.clasificacion")
    List<Object[]> totalesDelMes(@Param("idEstudiante") Long idEstudiante,
                                 @Param("inicio") LocalDate inicio,
                                 @Param("fin") LocalDate fin);

    /**
     * END-DASH-03 (US-08): sumas del mes por dia y tipo. Cada fila es [fecha, tipo, suma].
     * Las semanas (1-7, 8-14, ...) se arman en InicioServiceImpl.
     */
    @Query("SELECT m.fecha, m.tipo, SUM(m.monto) FROM Movimiento m " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY m.fecha, m.tipo " +
            "ORDER BY m.fecha")
    List<Object[]> evolucionSemanal(@Param("idEstudiante") Long idEstudiante,
                                    @Param("inicio") LocalDate inicio,
                                    @Param("fin") LocalDate fin);

    /**
     * END-DASH-03 (US-08): total del mes por categoria para un tipo (INGRESO o GASTO), de mayor a menor.
     * Cada fila es [nombreCategoria, suma]. Incluye categorias desactivadas si tienen movimientos.
     */
    @Query("SELECT m.categoria.nombre, SUM(m.monto) FROM Movimiento m " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante " +
            "AND m.tipo = :tipo " +
            "AND m.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY m.categoria.idCategoria, m.categoria.nombre " +
            "ORDER BY SUM(m.monto) DESC")
    List<Object[]> totalesPorCategoria(@Param("idEstudiante") Long idEstudiante,
                                       @Param("tipo") TipoMovimiento tipo,
                                       @Param("inicio") LocalDate inicio,
                                       @Param("fin") LocalDate fin);

    List<Movimiento> findBySuscripcionIdSuscripcionOrderByFechaDesc(Long idSuscripcion);

    boolean existsBySuscripcionIdSuscripcionAndTipoAndFechaGreaterThanEqual(
            Long idSuscripcion, String tipo, LocalDate fecha);
}
