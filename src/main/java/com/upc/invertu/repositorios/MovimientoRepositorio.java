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
}
