package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.CompraDTO;
import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompraController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompraService compraService;

    private CompraDTO compraDTO;

    @BeforeEach
    void setUp() {
        compraDTO = new CompraDTO();
        compraDTO.setId(1L);
        compraDTO.setClienteId(1L);
        compraDTO.setViajeId(1L);
        compraDTO.setTiendaId(1L);
        compraDTO.setEmpleadoId(1L);
        compraDTO.setPrecioFinal(new BigDecimal("1200.00"));
        compraDTO.setFechaCompra(LocalDateTime.now());
        compraDTO.setCompraOnline(false);

        AcompananteDTO acompananteDTO = new AcompananteDTO();
        acompananteDTO.setNombre("María García");
        acompananteDTO.setDni("87654321B");
        compraDTO.setAcompanantes(Arrays.asList(acompananteDTO));
    }

    @Test
    void testListarTodasLasCompras() throws Exception {
        // Given
        CompraDTO compraDTO2 = new CompraDTO();
        compraDTO2.setId(2L);
        compraDTO2.setClienteId(2L);
        compraDTO2.setViajeId(1L);
        compraDTO2.setPrecioFinal(new BigDecimal("900.00"));
        compraDTO2.setCompraOnline(true);
        List<CompraDTO> compras = Arrays.asList(compraDTO, compraDTO2);
        when(compraService.listarCompras()).thenReturn(compras);

        // When & Then
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].precioFinal", is(900.00)));
    }

    @Test
    void testObtenerCompra() throws Exception {
        // Given
        when(compraService.obtenerCompraPorId(1L)).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(get("/api/compras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clienteId", is(1)));
    }

    @Test
    void testRealizarCompra() throws Exception {
        // Given
        when(compraService.realizarCompra(any(CompraDTO.class))).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.clienteId", is(1)))
                .andExpect(jsonPath("$.viajeId", is(1)))
                .andExpect(jsonPath("$.precioFinal", is(1200.00)))
                .andExpect(jsonPath("$.compraOnline", is(false)))
                .andExpect(jsonPath("$.acompanantes", hasSize(1)))
                .andExpect(jsonPath("$.acompanantes[0].nombre", is("María García")));

        verify(compraService, times(1)).realizarCompra(any(CompraDTO.class));
    }

    @Test
    void testRealizarCompraOnline() throws Exception {
        // Given
        compraDTO.setCompraOnline(true);
        compraDTO.setTiendaId(null);
        compraDTO.setEmpleadoId(null);

        when(compraService.realizarCompra(any(CompraDTO.class))).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compraOnline", is(true)))
                .andExpect(jsonPath("$.tiendaId").doesNotExist())
                .andExpect(jsonPath("$.empleadoId").doesNotExist());
    }

    @Test
    void testRealizarCompra_ClienteNoEncontrado() throws Exception {
        // Given
        when(compraService.realizarCompra(any(CompraDTO.class)))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRealizarCompra_ViajeNoEncontrado() throws Exception {
        // Given
        when(compraService.realizarCompra(any(CompraDTO.class)))
                .thenThrow(new ResourceNotFoundException("Viaje no encontrado"));

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRealizarCompra_SinAcompanantes() throws Exception {
        // Given
        compraDTO.setAcompanantes(null);
        when(compraService.realizarCompra(any(CompraDTO.class))).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acompanantes").doesNotExist());
    }

    @Test
    void testRealizarCompra_ConDescuentoLunaMiel() throws Exception {
        // Given
        compraDTO.setPrecioFinal(new BigDecimal("1000.00")); // Precio con descuento
        when(compraService.realizarCompra(any(CompraDTO.class))).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioFinal", is(1000.00)));
    }
}
