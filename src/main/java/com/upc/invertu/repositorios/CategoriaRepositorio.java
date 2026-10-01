package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

    /** Usado por la carga inicial para no duplicar categorias predeterminadas. */
    boolean existsByNombreAndEstudianteIsNull(String nombre);
    /** END-CAT-01: predeterminadas (sin estudiante) + personalizadas del estudiante, solo activas. */
    @Query("SELECT c FROM Categoria c LEFT JOIN c.estudiante e " +
            "WHERE c.activa = true AND (e IS NULL OR e.idEstudiante = :idEstudiante) " +
            "ORDER BY c.nombre")
    List<Categoria> findDisponibles(@Param("idEstudiante") Long idEstudiante);

    /** END-CAT-02: el nombre no puede repetirse (sin distinguir mayusculas). */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Categoria c LEFT JOIN c.estudiante e " +
            "WHERE LOWER(c.nombre) = LOWER(:nombre) AND c.activa = true " +
            "AND (e IS NULL OR e.idEstudiante = :idEstudiante)")
    boolean existeNombre(@Param("nombre") String nombre, @Param("idEstudiante") Long idEstudiante);

    /** END-CAT-03: solo encuentra categorias personalizadas, activas y del propio estudiante. */
    Optional<Categoria> findByIdCategoriaAndEstudianteIdEstudianteAndActivaTrue(Long idCategoria, Long idEstudiante);
}
