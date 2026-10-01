package com.upc.invertu.entidades;

import com.upc.invertu.seguridad.entidades.Estudiante;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.entidades.enums.FrecuenciaAporte;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Objetivo de ahorro del estudiante. Su progreso se calcula con sus aportes.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "meta")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_meta")
    private Long idMeta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "monto_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "fecha_objetivo", nullable = false)
    private LocalDate fechaObjetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia_aporte", nullable = false, length = 20)
    private FrecuenciaAporte frecuenciaAporte = FrecuenciaAporte.SEMANAL;

    @Column(name = "proxima_fecha_aporte")
    private LocalDate proximaFechaAporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMeta estado = EstadoMeta.ACTIVA;

    @Column(name = "fecha_cumplimiento")
    private LocalDate fechaCumplimiento;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
