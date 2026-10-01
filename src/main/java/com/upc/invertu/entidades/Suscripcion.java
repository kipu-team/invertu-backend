package com.upc.invertu.entidades;

import com.upc.invertu.seguridad.entidades.Estudiante;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.FrecuenciaSuscripcion;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicios con cobro recurrente y la configuracion de sus avisos por correo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long idSuscripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrecuenciaSuscripcion frecuencia;

    @Column(name = "proxima_fecha_cobro", nullable = false)
    private LocalDate proximaFechaCobro;

    @Column(name = "recordatorio_activo", nullable = false)
    private Boolean recordatorioActivo = false;

    @Column(name = "dias_anticipacion")
    private Integer diasAnticipacion;

    @Column(name = "ultima_fecha_recordatorio")
    private LocalDate ultimaFechaRecordatorio;

    @Column(name = "alerta_saldo_activa", nullable = false)
    private Boolean alertaSaldoActiva = false;

    @Column(name = "ultima_fecha_alerta_saldo")
    private LocalDate ultimaFechaAlertaSaldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSuscripcion estado = EstadoSuscripcion.ACTIVA;

    @Column(name = "fecha_cancelacion")
    private LocalDate fechaCancelacion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
