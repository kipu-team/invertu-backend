package com.upc.invertu.seguridad.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Roles de seguridad del sistema (patron Spring Security).
 * Valores: ROLE_FREE, ROLE_PREMIUM.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
