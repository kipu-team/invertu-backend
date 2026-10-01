package com.upc.invertu.unitarias;

import com.upc.invertu.excepciones.ConflictoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.seguridad.dtos.request.RegistroRequestDTO;
import com.upc.invertu.seguridad.dtos.response.RegistroResponseDTO;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.seguridad.repositorios.EstudianteRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import com.upc.invertu.seguridad.serviciosimpl.AuthServiceImpl;
import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** US-01: reglas de negocio del registro (END-AUTH-01) */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EstudianteRepositorio estudianteRepositorio;

    @Mock
    private RolRepositorio rolRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistroRequestDTO request(String correo, String contrasena, String confirmar) {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNombres("  Ana  ");
        dto.setApellidos(" Perez ");
        dto.setCorreo(correo);
        dto.setContrasena(contrasena);
        dto.setConfirmarContrasena(confirmar);
        return dto;
    }

    @Test
    void registroExitoso_creaEstudianteFreeConCorreoEnMinusculasYContrasenaCifrada() {
        when(estudianteRepositorio.existsByCorreo("ana@upc.edu.pe")).thenReturn(false);
        when(rolRepositorio.findByNombre("ROLE_FREE")).thenReturn(Optional.of(new Rol("ROLE_FREE")));
        when(passwordEncoder.encode("Clave123!")).thenReturn("hash-bcrypt");
        when(estudianteRepositorio.save(any(Estudiante.class))).thenAnswer(inv -> {
            Estudiante e = inv.getArgument(0);
            e.setIdEstudiante(1L);
            return e;
        });

        RegistroResponseDTO respuesta = authService.registrar(request(" Ana@UPC.edu.pe ", "Clave123!", "Clave123!"));

        ArgumentCaptor<Estudiante> captor = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepositorio).save(captor.capture());
        Estudiante guardado = captor.getValue();
        assertEquals("ana@upc.edu.pe", guardado.getCorreo());
        assertEquals("Ana", guardado.getNombres());
        assertEquals("Perez", guardado.getApellidos());
        assertEquals("hash-bcrypt", guardado.getPasswordHash());
        assertEquals("ROLE_FREE", guardado.getRol().getNombre());
        assertEquals(Tema.SISTEMA, guardado.getTema());
        assertEquals(Idioma.es_419, guardado.getIdioma());
        assertNull(guardado.getUniversidad());

        assertEquals(1L, respuesta.getIdEstudiante());
        assertEquals("Ana", respuesta.getNombres());
        assertEquals("ana@upc.edu.pe", respuesta.getCorreo());
        assertEquals("ROLE_FREE", respuesta.getRol());
    }

    @Test
    void correoYaRegistrado_lanza409() {
        when(estudianteRepositorio.existsByCorreo("ana@upc.edu.pe")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> authService.registrar(request("ANA@upc.edu.pe", "Clave123!", "Clave123!")));

        assertEquals("Este correo ya está registrado", ex.getMessage());
        verify(estudianteRepositorio, never()).save(any());
    }

    @Test
    void contrasenasDistintas_lanza400SinConsultarNiGuardar() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> authService.registrar(request("ana@upc.edu.pe", "Clave123!", "Clave123?")));

        assertEquals("Las contraseñas no coinciden", ex.getMessage());
        verifyNoInteractions(estudianteRepositorio, rolRepositorio, passwordEncoder);
    }
}
