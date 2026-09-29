package com.Dev.UvTours.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ViajeDTO {

    private Long id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal precio;
    private Boolean esLunaMiel;
    private BigDecimal descuentoLunaMiel;
    private String tipoViaje; // "SIMPLE" o "CIRCUITO"
    private String destino; // Solo para viajes simples
    private List<Long> viajesSimplesIds; // Solo para circuitos
    private List<ActividadDTO> actividades;
}
