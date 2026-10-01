package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Aporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

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
}
