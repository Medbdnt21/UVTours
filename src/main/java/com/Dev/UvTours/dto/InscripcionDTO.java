package com.Dev.UvTours.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InscripcionDTO {

    private Long id;
    private Long clienteId;
    private Long actividadId;
    private LocalDateTime fechaInscripcion;
}