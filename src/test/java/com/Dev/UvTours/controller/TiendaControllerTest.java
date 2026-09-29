package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.TiendaDTO;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.service.TiendaService;
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

@WebMvcTest(TiendaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TiendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TiendaService tiendaService;

    private TiendaDTO tiendaDTO;

    @BeforeEach
    void setUp() {
        tiendaDTO = new TiendaDTO();
        tiendaDTO.setId(1L);
        tiendaDTO.setDireccion("Campus Universitario 1");
        tiendaDTO.setCodigoPostal("28001");
        tiendaDTO.setTelefono("911234567");
    }

    @Test
    void testListarTodasLasTiendas() throws Exception {
        // Given
        TiendaDTO tiendaDTO2 = new TiendaDTO();
        tiendaDTO2.setId(2L);
        tiendaDTO2.setDireccion("Campus Universitario 2");
        tiendaDTO2.setCodigoPostal("46001");
        tiendaDTO2.setTelefono("963456789");
        List<TiendaDTO> tiendas = Arrays.asList(tiendaDTO, tiendaDTO2);
        when(tiendaService.listarTodasLasTiendas()).thenReturn(tiendas);

        // When & Then
        mockMvc.perform(get("/api/tiendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].direccion", is("Campus Universitario 1")));
    }

    @Test
    void testObtenerTienda() throws Exception {
        // Given
        when(tiendaService.obtenerTiendaPorId(1L)).thenReturn(tiendaDTO);

        // When & Then
        mockMvc.perform(get("/api/tiendas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.telefono", is("911234567")));
    }

    @Test
    void testCrearTienda() throws Exception {
        // Given
        when(tiendaService.crearTienda(any(TiendaDTO.class))).thenReturn(tiendaDTO);

        // When & Then
        mockMvc.perform(post("/api/tiendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tiendaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.codigoPostal", is("28001")));
    }

    @Test
    void testActualizarTienda() throws Exception {
        // Given
        when(tiendaService.actualizarTienda(eq(1L), any(TiendaDTO.class))).thenReturn(tiendaDTO);

        // When & Then
        mockMvc.perform(put("/api/tiendas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tiendaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testEliminarTienda() throws Exception {
        // Given
        doNothing().when(tiendaService).eliminarTienda(1L);

        // When & Then
        mockMvc.perform(delete("/api/tiendas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testObtenerTienda_NoEncontrada() throws Exception {
        // Given
        when(tiendaService.obtenerTiendaPorId(999L))
                .thenThrow(new ResourceNotFoundException("Tienda no encontrada con id: 999"));

        // When & Then
        mockMvc.perform(get("/api/tiendas/999"))
                .andExpect(status().isNotFound());
    }
}