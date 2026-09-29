package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.InscripcionDTO;
import com.Dev.UvTours.service.InscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InscripcionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InscripcionService inscripcionService;

    private InscripcionDTO inscripcionDTO;

    @BeforeEach
    void setUp() {
        inscripcionDTO = new InscripcionDTO();
        inscripcionDTO.setId(1L);
        inscripcionDTO.setClienteId(1L);
        inscripcionDTO.setActividadId(1L);
        inscripcionDTO.setFechaInscripcion(LocalDateTime.now());
    }

    @Test
    void testInscribir() throws Exception {
        // Given
        when(inscripcionService.inscribir(eq(1L), eq(1L))).thenReturn(inscripcionDTO);

        // When & Then
        mockMvc.perform(post("/api/inscripciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inscripcionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clienteId", is(1)))
                .andExpect(jsonPath("$.actividadId", is(1)));
    }

    @Test
    void testListarPorActividad() throws Exception {
        // Given
        List<InscripcionDTO> inscripciones = Arrays.asList(inscripcionDTO);
        when(inscripcionService.listarPorActividad(1L)).thenReturn(inscripciones);

        // When & Then
        mockMvc.perform(get("/api/inscripciones")
                .param("actividadId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId", is(1)));
    }

    @Test
    void testListarPorCliente() throws Exception {
        // Given
        List<InscripcionDTO> inscripciones = Arrays.asList(inscripcionDTO);
        when(inscripcionService.listarPorCliente(1L)).thenReturn(inscripciones);

        // When & Then
        mockMvc.perform(get("/api/inscripciones")
                .param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].actividadId", is(1)));
    }

    @Test
    void testAnularInscripcion() throws Exception {
        // Given
        doNothing().when(inscripcionService).anularInscripcion(1L);

        // When & Then
        mockMvc.perform(delete("/api/inscripciones/1"))
                .andExpect(status().isNoContent());
    }
}