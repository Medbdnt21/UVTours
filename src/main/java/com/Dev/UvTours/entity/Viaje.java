package com.Dev.UvTours.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "viajes")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    private BigDecimal precio;

    @Column(name = "es_luna_miel")
    private Boolean esLunaMiel = false;

    @Column(name = "descuento_luna_miel")
    private BigDecimal descuentoLunaMiel = BigDecimal.ZERO;

    @ManyToMany
    @JoinTable(
            name = "viaje_actividad",
            joinColumns = @JoinColumn(name = "viaje_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private List<Actividad> actividades = new ArrayList<>();

    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
}
