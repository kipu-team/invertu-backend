package com.upc.invertu.seguridad.serviciosimpl;

import com.upc.invertu.entidades.Plan;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.servicios.EstudianteService;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstudianteServiceImpl implements EstudianteService {
    // TODO: implementar las reglas de negocio

    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private PlanRepositorio planRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-PROF-01: perfil del estudiante autenticado con sus preferencias, rol y plan vigente */
    @Override
    @Transactional(readOnly = true)
    public PerfilResponseDTO consultarPerfil() {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        Estudiante estudiante = estudianteRepositorio.findByIdConRol(idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        String rol = estudiante.getRol().getNombre();
        Plan plan = planRepositorio.findByRolIdRol(estudiante.getRol().getIdRol())
                .orElseThrow(() -> new IllegalStateException("No existe un plan para el rol " + rol));

        PerfilResponseDTO dto = new PerfilResponseDTO();
        dto.setNombres(estudiante.getNombres());
        dto.setApellidos(estudiante.getApellidos());
        dto.setCorreo(estudiante.getCorreo());
        dto.setUniversidad(estudiante.getUniversidad());
        dto.setTema(estudiante.getTema());
        dto.setIdioma(estudiante.getIdioma());
        dto.setRol(rol);

        PerfilResponseDTO.PlanDTO planDTO = new PerfilResponseDTO.PlanDTO();
        planDTO.setPrecioMensual(plan.getPrecioMensual());
        planDTO.setMaxMetasActivas(plan.getMaxMetasActivas());
        planDTO.setMaxRecordatorios(plan.getMaxRecordatorios());
        dto.setPlan(planDTO);
        return dto;
    }
}
