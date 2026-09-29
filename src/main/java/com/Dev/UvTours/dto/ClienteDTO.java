package com.Dev.UvTours.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;
    private List<AcompananteDTO> acompanantes;
}
