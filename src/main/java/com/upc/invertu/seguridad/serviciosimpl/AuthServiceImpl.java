package com.upc.invertu.seguridad.serviciosimpl;

import com.upc.invertu.configuracion.DataInitializer;
import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private EstudianteRepositorio estudianteRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** END-AUTH-01: registra un estudiante con rol FREE (tema SISTEMA e idioma es_419 por defecto) */
    @Override
    @Transactional
    public RegistroResponseDTO registrar(RegistroRequestDTO dto) {
        if (!dto.getContrasena().equals(dto.getConfirmarContrasena())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }

        // El correo se guarda en minusculas: "Ana@UPC.edu.pe" y "ana@upc.edu.pe" son el mismo
        String correo = dto.getCorreo().trim().toLowerCase();
        if (estudianteRepositorio.existsByCorreo(correo)) {
            throw new ConflictoException("Este correo ya está registrado");
        }

        // DataInitializer crea el rol al iniciar; si faltara es un error de configuracion (500)
        Rol rolFree = rolRepositorio.findByNombre(DataInitializer.ROLE_FREE)
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + DataInitializer.ROLE_FREE));

        Estudiante estudiante = new Estudiante();
        estudiante.setNombres(dto.getNombres().trim());
        estudiante.setApellidos(dto.getApellidos().trim());
        estudiante.setCorreo(correo);
        estudiante.setPasswordHash(passwordEncoder.encode(dto.getContrasena())); // BCrypt
        estudiante.setRol(rolFree);
        estudiante = estudianteRepositorio.save(estudiante);

        return new RegistroResponseDTO(
                estudiante.getIdEstudiante(),
                estudiante.getNombres(),
                estudiante.getCorreo(),
                rolFree.getNombre());
    }
}
