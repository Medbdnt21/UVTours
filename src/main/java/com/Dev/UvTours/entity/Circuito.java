package com.Dev.UvTours.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "circuitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Circuito extends Viaje {

    @ManyToMany
    @JoinTable(
            name = "circuito_viajes_simples",
            joinColumns = @JoinColumn(name = "circuito_id"),
            inverseJoinColumns = @JoinColumn(name = "viaje_simple_id")
    )
    private List<ViajeSimple> viajesSimples = new ArrayList<>();

    public BigDecimal calcularCosteTotal() {
        return viajesSimples.stream()
                .map(Viaje::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
