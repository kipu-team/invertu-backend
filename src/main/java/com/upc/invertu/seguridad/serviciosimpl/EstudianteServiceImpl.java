package com.upc.invertu.seguridad.serviciosimpl;

import com.upc.invertu.dtos.response.MensajeResponseDTO;
import com.upc.invertu.entidades.Plan;
import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.seguridad.dtos.request.CambiarContrasenaRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PerfilRequestDTO;
import com.upc.invertu.seguridad.dtos.request.PreferenciasRequestDTO;
import com.upc.invertu.seguridad.dtos.response.PerfilResponseDTO;
import com.upc.invertu.seguridad.dtos.response.PreferenciasResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.servicios.EstudianteService;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Mismos requisitos que en el registro (END-AUTH-01)
    private static final String POLITICA_CONTRASENA = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$";

    /** END-PROF-01: perfil del estudiante autenticado con sus preferencias, rol y plan vigente */
    @Override
    @Transactional(readOnly = true)
    public PerfilResponseDTO consultarPerfil() {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        Estudiante estudiante = estudianteRepositorio.findByIdConRol(idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
        return aPerfilDTO(estudiante);
    }

    /**
     * END-PROF-02: actualiza nombres, apellidos y universidad del estudiante autenticado.
     * El correo, el rol y el resto de sus datos no se tocan.
     */
    @Override
    @Transactional
    public PerfilResponseDTO actualizarPerfil(PerfilRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        estudiante.setNombres(dto.getNombres().trim());
        estudiante.setApellidos(dto.getApellidos().trim());
        estudiante.setUniversidad(limpiarTexto(dto.getUniversidad()));
        estudianteRepositorio.save(estudiante);
        return aPerfilDTO(estudiante);
    }

    /**
     * END-PROF-03: actualiza tema e idioma del estudiante autenticado.
     * Solo cambian esas dos preferencias y la fecha de modificacion.
     */
    @Override
    @Transactional
    public PreferenciasResponseDTO actualizarPreferencias(PreferenciasRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        Tema tema;
        Idioma idioma;
        try {
            tema = Tema.valueOf(dto.getTema());
            idioma = Idioma.valueOf(dto.getIdioma());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ReglaNegocioException("Tema o idioma no válido");
        }

        estudianteRepositorio.actualizarPreferencias(estudiante.getIdEstudiante(), tema, idioma);

        PreferenciasResponseDTO respuesta = new PreferenciasResponseDTO();
        respuesta.setTema(tema);
        respuesta.setIdioma(idioma);
        return respuesta;
    }

    /** END-PROF-04: cambia la contrasena del estudiante autenticado (BCrypt) */
    @Override
    @Transactional
    public MensajeResponseDTO cambiarContrasena(CambiarContrasenaRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        if (dto.getContrasenaActual() == null
                || !passwordEncoder.matches(dto.getContrasenaActual(), estudiante.getPasswordHash())) {
            throw new ReglaNegocioException("La contraseña actual ingresada es incorrecta");
        }

        String nuevaContrasena = dto.getNuevaContrasena();
        if (nuevaContrasena == null || !nuevaContrasena.matches(POLITICA_CONTRASENA)) {
            throw new ReglaNegocioException("La nueva contraseña no cumple los requisitos de seguridad");
        }

        if (nuevaContrasena.equals(dto.getContrasenaActual())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta a la actual");
        }

        if (!nuevaContrasena.equals(dto.getConfirmarContrasena())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }

        estudiante.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        estudianteRepositorio.save(estudiante);
        return new MensajeResponseDTO("Tu contraseña se actualizó correctamente");
    }

    /** Datos personales, preferencias y plan vigente; compartido por END-PROF-01 y 02 */
    private PerfilResponseDTO aPerfilDTO(Estudiante estudiante) {
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

    // Texto opcional: si viene vacio o con solo espacios se guarda null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
