package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Aporte;
import com.upc.invertu.entidades.enums.EstadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AporteRepositorio extends JpaRepository<Aporte, Long> {
    /**
     * END-GOAL-03 y 04: total aportado a cada meta del estudiante, en una sola consulta.
     * Cada fila es [idMeta, suma]. Las metas sin aportes no aparecen (su total es 0).
     */
    @Query("SELECT a.meta.idMeta, SUM(a.monto) FROM Aporte a " +
            "WHERE a.meta.estudiante.idEstudiante = :idEstudiante " +
            "GROUP BY a.meta.idMeta")
    List<Object[]> aportadoPorMeta(@Param("idEstudiante") Long idEstudiante);
    /** END-GOAL-05, 07 y 12: total aportado a una meta (null si no tiene aportes). */
    @Query("SELECT SUM(a.monto) FROM Aporte a WHERE a.meta.idMeta = :idMeta")
    BigDecimal sumarAportes(@Param("idMeta") Long idMeta);
    /** END-GOAL-06 y 07: aportes de una meta, del mas antiguo al mas reciente. */
    List<Aporte> findByMetaIdMetaOrderByFechaAsc(Long idMeta);
    /** END-GOAL-13: valida en una sola consulta que el aporte exista, sea de esa meta y que la meta sea del estudiante.*/
    Optional<Aporte> findByIdAporteAndMetaIdMetaAndMetaEstudianteIdEstudiante(Long idAporte, Long idMeta,
                                                                              Long idEstudiante);
    /** END-GOAL-11: verifica si la meta tiene aportes registrados. */
    boolean existsByMetaIdMeta(Long idMeta);

    /** END-DASH-02 (US-07): suma de los aportes del estudiante registrados en el mes (0 si no hay). */
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM Aporte a " +
            "WHERE a.meta.estudiante.idEstudiante = :idEstudiante " +
            "AND a.fecha BETWEEN :inicio AND :fin")
    BigDecimal aportesDelMes(@Param("idEstudiante") Long idEstudiante,
                             @Param("inicio") LocalDate inicio,
                             @Param("fin") LocalDate fin);

    /** END-DASH-02 (US-07): suma de todos los aportes de las metas en los estados indicados (0 si no hay). */
    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM Aporte a " +
            "WHERE a.meta.estudiante.idEstudiante = :idEstudiante " +
            "AND a.meta.estado IN :estados")
    BigDecimal ahorroTotal(@Param("idEstudiante") Long idEstudiante,
                           @Param("estados") List<EstadoMeta> estados);
}
