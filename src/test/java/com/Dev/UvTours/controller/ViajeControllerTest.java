package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.ViajeDTO;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.service.ViajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ViajeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ViajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ViajeService viajeService;

    private ViajeDTO viajeDTO;
    private List<ViajeDTO> viajesDTO;

    @BeforeEach
    void setUp() {
        viajeDTO = new ViajeDTO();
        viajeDTO.setId(1L);
        viajeDTO.setNombre("Viaje a París");
        viajeDTO.setFechaInicio(LocalDate.of(2026, 8, 15));
        viajeDTO.setFechaFin(LocalDate.of(2026, 8, 22));
        viajeDTO.setPrecio(new BigDecimal("1200.00"));
        viajeDTO.setTipoViaje("SIMPLE");
        viajeDTO.setDestino("París");
        viajeDTO.setEsLunaMiel(false);

        ViajeDTO viajeDTO2 = new ViajeDTO();
        viajeDTO2.setId(2L);
        viajeDTO2.setNombre("Circuito Europa");
        viajeDTO2.setFechaInicio(LocalDate.of(2026, 9, 1));
        viajeDTO2.setFechaFin(LocalDate.of(2026, 9, 15));
        viajeDTO2.setPrecio(new BigDecimal("2500.00"));
        viajeDTO2.setTipoViaje("CIRCUITO");
        viajeDTO2.setEsLunaMiel(true);

        viajesDTO = Arrays.asList(viajeDTO, viajeDTO2);
    }

    @Test
    void testListarTodosLosViajes() throws Exception {
        // Given
        when(viajeService.listarTodosLosViajes()).thenReturn(viajesDTO);

        // When & Then
        mockMvc.perform(get("/api/viajes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Viaje a París")))
                .andExpect(jsonPath("$[1].nombre", is("Circuito Europa")));

        verify(viajeService, times(1)).listarTodosLosViajes();
    }

    @Test
    void testCrearViaje() throws Exception {
        // Given
        when(viajeService.crearViaje(any(ViajeDTO.class))).thenReturn(viajeDTO);

        // When & Then
        mockMvc.perform(post("/api/viajes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(viajeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Viaje a París")))
                .andExpect(jsonPath("$.tipoViaje", is("SIMPLE")));

        verify(viajeService, times(1)).crearViaje(any(ViajeDTO.class));
    }

    @Test
    void testActualizarViaje() throws Exception {
        // Given
        when(viajeService.actualizarViaje(eq(1L), any(ViajeDTO.class))).thenReturn(viajeDTO);

        // When & Then
        mockMvc.perform(put("/api/viajes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(viajeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Viaje a París")));

        verify(viajeService, times(1)).actualizarViaje(eq(1L), any(ViajeDTO.class));
    }

    @Test
    void testEliminarViaje() throws Exception {
        // Given
        doNothing().when(viajeService).eliminarViaje(1L);

        // When & Then
        mockMvc.perform(delete("/api/viajes/1"))
                .andExpect(status().isNoContent());

        verify(viajeService, times(1)).eliminarViaje(1L);
    }

    @Test
    void testActualizarDescuentoLunaMiel() throws Exception {
        // Given
        when(viajeService.actualizarDescuentoLunaMiel(eq(1L), any(BigDecimal.class)))
                .thenReturn(viajeDTO);

        // When & Then
        mockMvc.perform(patch("/api/viajes/1/descuento-luna-miel")
                .param("descuento", "100.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(viajeService, times(1))
                .actualizarDescuentoLunaMiel(eq(1L), any(BigDecimal.class));
    }

    @Test
    void testActualizarViaje_NotFound() throws Exception {
        // Given
        when(viajeService.actualizarViaje(eq(999L), any(ViajeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Viaje no encontrado con id: 999"));

        // When & Then
        mockMvc.perform(put("/api/viajes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(viajeDTO)))
                .andExpect(status().isNotFound());
    }
}
