package com.Dev.UvTours.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "viajes_simples")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ViajeSimple extends Viaje {

    @Column(nullable = false)
    private String destino;

    @Column(name = "destino_unico")
    private Boolean destinoUnico = true;
}
