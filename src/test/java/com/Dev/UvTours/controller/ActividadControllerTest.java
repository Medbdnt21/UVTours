package com.Dev.UvTours.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Dev.UvTours.dto.ActividadDTO;
import com.Dev.UvTours.dto.ActividadDelDiaDTO;
import com.Dev.UvTours.service.ActividadService;

import tools.jackson.databind.ObjectMapper;

// @WebMvcTest(ActividadController.class) is used to test the ActividadController class in isolation,
//  without starting the entire Spring context.
//  It configures only the web layer and allows you to test the controller's endpoints and their behavior.
@WebMvcTest(ActividadController.class)
// @AutoConfigureMockMvc(addFilters = false) is used to configure the MockMvc instance for testing the controller's
//  endpoints. so that you can perform HTTP requests and assert the responses without starting a real server.
@AutoConfigureMockMvc(addFilters = false)
class ActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActividadService actividadService;

    private ActividadDTO actividadDTO;
    private List<ActividadDTO> actividadesDTO;

    @BeforeEach
    void setUp() {
        actividadDTO = new ActividadDTO();
        actividadDTO.setId(1L);
        actividadDTO.setNombre("Visita al Louvre");
        actividadDTO.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividadDTO.setEstado("PLANIFICADA");

        ActividadDTO actividadDTO2 = new ActividadDTO();
        actividadDTO2.setId(2L);
        actividadDTO2.setNombre("Paseo en barco por el Sena");
        actividadDTO2.setFechaActividad(LocalDate.of(2026, 8, 17));
        actividadDTO2.setEstado("PLANIFICADA");

        actividadesDTO = Arrays.asList(actividadDTO, actividadDTO2);
    }

    @Test
    void testListarTodasLasActividades() throws Exception {
        // Given
        when(actividadService.listarTodasLasActividades()).thenReturn(actividadesDTO);

        // When & Then
        mockMvc.perform(get("/api/actividades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Visita al Louvre")))
                .andExpect(jsonPath("$[1].nombre", is("Paseo en barco por el Sena")));
    }

    @Test
    void testCrearActividad() throws Exception {
        // Given
        when(actividadService.crearActividad(any(ActividadDTO.class))).thenReturn(actividadDTO);

        // When & Then
        mockMvc.perform(post("/api/actividades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actividadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Visita al Louvre")))
                .andExpect(jsonPath("$.estado", is("PLANIFICADA")));
    }

    @Test
    void testCambiarEstadoActividad() throws Exception {
        // Given
        ActividadDTO actividadEnEjecucion = new ActividadDTO();
        actividadEnEjecucion.setId(1L);
        actividadEnEjecucion.setNombre("Visita al Louvre");
        actividadEnEjecucion.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividadEnEjecucion.setEstado("EN_EJECUCION");

        when(actividadService.cambiarEstadoActividad(eq(1L), eq("EN_EJECUCION")))
                .thenReturn(actividadEnEjecucion);

        // When & Then
        mockMvc.perform(patch("/api/actividades/1/estado")
                .param("estado", "EN_EJECUCION")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_EJECUCION")));
    }

    @Test
    void testEliminarActividad() throws Exception {
        // Given
        doNothing().when(actividadService).eliminarActividad(1L);

        // When & Then
        mockMvc.perform(delete("/api/actividades/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testListarActividadesDelDia() throws Exception {
        // Given
        ActividadDelDiaDTO actividadDelDia = new ActividadDelDiaDTO();
        actividadDelDia.setId(1L);
        actividadDelDia.setFecha(LocalDate.of(2026, 8, 16));
        actividadDelDia.setActividadId(1L);
        actividadDelDia.setNombre("Visita al Louvre");
        actividadDelDia.setEstado("PLANIFICADA");
        List<ActividadDelDiaDTO> lista = Arrays.asList(actividadDelDia);
        when(actividadService.listarActividadesDelDia(LocalDate.of(2026, 8, 16))).thenReturn(lista);

        // When & Then
        mockMvc.perform(get("/api/actividades/hoy")
                .param("fecha", "2026-08-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Visita al Louvre")))
                .andExpect(jsonPath("$[0].estado", is("PLANIFICADA")));
    }
}
