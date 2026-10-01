package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Aporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
