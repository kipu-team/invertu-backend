package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepositorio extends JpaRepository<Movimiento, Long> {
    /**
     * END-TRX-01 (US-10): movimientos del mes del estudiante, del mas reciente al mas antiguo.
     * JOIN FETCH trae la categoria y la suscripcion en la misma consulta.
     * Los filtros de busqueda se agregan en T-17 (US-14).
     */
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.categoria LEFT JOIN FETCH m.suscripcion " +
            "WHERE m.estudiante.idEstudiante = :idEstudiante AND m.fecha BETWEEN :inicio AND :fin " +
            "ORDER BY m.fecha DESC, m.idMovimiento DESC")
    List<Movimiento> listarDelMes(@Param("idEstudiante") Long idEstudiante,
                                  @Param("inicio") LocalDate inicio,
                                  @Param("fin") LocalDate fin);

    /** END-TRX-04, 05 y 06: solo encuentra el movimiento si pertenece al estudiante. */
    Optional<Movimiento> findByIdMovimientoAndEstudianteIdEstudiante(Long idMovimiento, Long idEstudiante);
}
