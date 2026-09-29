package com.Dev.UvTours.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ActividadDTO {

    private Long id;
    private String nombre;
    private LocalDate fechaActividad;
    private String estado;
}
