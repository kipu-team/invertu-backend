package com.upc.invertu.repositorios;

import com.upc.invertu.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

    /** Usado por la carga inicial para no duplicar categorias predeterminadas. */
    boolean existsByNombreAndEstudianteIsNull(String nombre);
}
