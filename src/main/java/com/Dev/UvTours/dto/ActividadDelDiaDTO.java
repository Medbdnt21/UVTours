package com.Dev.UvTours.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActividadDelDiaDTO {

    private Long id;
    private LocalDate fecha;
    private Long actividadId;
    private String nombre;
    private String estado;
}