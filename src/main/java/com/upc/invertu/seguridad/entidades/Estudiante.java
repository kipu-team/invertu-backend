package com.upc.invertu.seguridad.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.upc.invertu.entidades.enums.Idioma;
import com.upc.invertu.entidades.enums.Tema;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Usuario del sistema. El correo cumple la funcion de username en Spring Security.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 150)
    private String universidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tema tema = Tema.SISTEMA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Idioma idioma = Idioma.es_419;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
