package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.EmpleadoDTO;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.service.EmpleadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpleadoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpleadoService empleadoService;

    private EmpleadoDTO empleadoDTO;

    @BeforeEach
    void setUp() {
        empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setId(1L);
        empleadoDTO.setNombre("Ana Martínez");
        empleadoDTO.setEmail("ana@uvtours.com");
        empleadoDTO.setDni("98765432C");
        empleadoDTO.setTiendaId(1L);
    }

    @Test
    void testListarTodosLosEmpleados() throws Exception {
        // Given
        EmpleadoDTO empleadoDTO2 = new EmpleadoDTO();
        empleadoDTO2.setId(2L);
        empleadoDTO2.setNombre("Carlos Ruiz");
        empleadoDTO2.setEmail("carlos@uvtours.com");
        empleadoDTO2.setDni("11111111E");
        empleadoDTO2.setTiendaId(1L);
        List<EmpleadoDTO> empleados = Arrays.asList(empleadoDTO, empleadoDTO2);
        when(empleadoService.listarTodosLosEmpleados()).thenReturn(empleados);

        // When & Then
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Ana Martínez")));
    }

    @Test
    void testObtenerEmpleado() throws Exception {
        // Given
        when(empleadoService.obtenerEmpleadoPorId(1L)).thenReturn(empleadoDTO);

        // When & Then
        mockMvc.perform(get("/api/empleados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tiendaId", is(1)));
    }

    @Test
    void testCrearEmpleado() throws Exception {
        // Given
        when(empleadoService.crearEmpleado(any(EmpleadoDTO.class))).thenReturn(empleadoDTO);

        // When & Then
        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empleadoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.dni", is("98765432C")));
    }

    @Test
    void testActualizarEmpleado() throws Exception {
        // Given
        when(empleadoService.actualizarEmpleado(eq(1L), any(EmpleadoDTO.class))).thenReturn(empleadoDTO);

        // When & Then
        mockMvc.perform(put("/api/empleados/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empleadoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testEliminarEmpleado() throws Exception {
        // Given
        doNothing().when(empleadoService).eliminarEmpleado(1L);

        // When & Then
        mockMvc.perform(delete("/api/empleados/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testObtenerEmpleado_NoEncontrado() throws Exception {
        // Given
        when(empleadoService.obtenerEmpleadoPorId(999L))
                .thenThrow(new ResourceNotFoundException("Empleado no encontrado con id: 999"));

        // When & Then
        mockMvc.perform(get("/api/empleados/999"))
                .andExpect(status().isNotFound());
    }
}