package com.Dev.UvTours.dto;

import lombok.Data;

import java.util.List;

@Data
public class TiendaDTO {

    private Long id;
    private String direccion;
    private String codigoPostal;
    private String telefono;
    private List<EmpleadoDTO> empleados;
}