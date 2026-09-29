package com.Dev.UvTours.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "actividades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDate fechaActividad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActividad estado = EstadoActividad.PLANIFICADA;

    @Column(name = "fecha_cancelacion")
    private LocalDate fechaCancelacion;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    public enum EstadoActividad {
        PLANIFICADA,
        EN_EJECUCION,
        FINALIZADA,
        CANCELADA
    }
}
