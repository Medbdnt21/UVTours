package com.Dev.UvTours.dto;

import lombok.Data;

@Data
public class EmpleadoDTO {

    private Long id;
    private String nombre;
    private String email;
    private String dni;
    private Long tiendaId;
}