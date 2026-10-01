package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.CategoriaRequestDTO;
import com.upc.invertu.dtos.response.CategoriaResponseDTO;
import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.entidades.Categoria;
import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.repositorios.CategoriaRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.CategoriaService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-CAT-01: predeterminadas + personalizadas activas del estudiante autenticado */
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarDisponibles() {
        // El estudiante sale del token, nunca del request
        Estudiante estudiante = estudianteAutenticado.obtener();
        return categoriaRepositorio.findDisponibles(estudiante.getIdEstudiante()).stream()
                .map(this::aDTO)
                .toList();
    }

    /** END-CAT-02: crea una categoria personalizada (solo Premium, validado en el controller) */
    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();
        // Se quitan los espacios de los extremos para que " Mascotas " y "Mascotas" sean iguales
        String nombre = dto.getNombre().trim();

        // No puede repetirse entre sus categorias ni con una predeterminada (sin distinguir mayusculas) -> 409
        if (categoriaRepositorio.existeNombre(nombre, estudiante.getIdEstudiante())) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setEstudiante(estudiante); // con estudiante = personalizada
        return aDTO(categoriaRepositorio.save(categoria));
    }

    /** END-CAT-03: eliminacion logica de una categoria personalizada */
    @Override
    @Transactional
    public MensajeResponseDTO desactivar(Long idCategoria) {
        Estudiante estudiante = estudianteAutenticado.obtener();
        // Solo encuentra categorias propias y activas: las predeterminadas o de otro estudiante dan 404
        Categoria categoria = categoriaRepositorio
                .findByIdCategoriaAndEstudianteIdEstudianteAndActivaTrue(idCategoria, estudiante.getIdEstudiante())
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría no existe o no puede eliminarse"));

        // No se borra: los movimientos anteriores conservan su categoria
        categoria.setActiva(false);
        categoriaRepositorio.save(categoria);
        return new MensajeResponseDTO("Categoría eliminada");
    }

    // No se usa ModelMapper porque "personalizada" no es un campo de la entidad, se calcula
    private CategoriaResponseDTO aDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getEstudiante() != null); // predeterminada = sin estudiante
    }
}
