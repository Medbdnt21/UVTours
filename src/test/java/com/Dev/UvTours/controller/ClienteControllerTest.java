package com.Dev.UvTours.controller;

import tools.jackson.databind.ObjectMapper;
import com.Dev.UvTours.dto.ClienteDTO;
import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        clienteDTO = new ClienteDTO();
        clienteDTO.setId(1L);
        clienteDTO.setDni("12345678A");
        clienteDTO.setNombre("Juan Pérez");
        clienteDTO.setDireccion("Calle Mayor 1, Madrid");
        clienteDTO.setEmail("juan@email.com");
        clienteDTO.setTelefono("600123456");

        AcompananteDTO acompananteDTO = new AcompananteDTO();
        acompananteDTO.setId(1L);
        acompananteDTO.setNombre("María García");
        acompananteDTO.setDni("87654321B");
        clienteDTO.setAcompanantes(Arrays.asList(acompananteDTO));
    }

    @Test
    void testCrearCliente() throws Exception {
        // Given
        when(clienteService.crearCliente(any(ClienteDTO.class))).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Juan Pérez")))
                .andExpect(jsonPath("$.dni", is("12345678A")))
                .andExpect(jsonPath("$.email", is("juan@email.com")))
                .andExpect(jsonPath("$.acompanantes", hasSize(1)))
                .andExpect(jsonPath("$.acompanantes[0].nombre", is("María García")));

        verify(clienteService, times(1)).crearCliente(any(ClienteDTO.class));
    }

    @Test
    void testObtenerClientePorId() throws Exception {
        // Given
        when(clienteService.obtenerClientePorId(1L)).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(get("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Juan Pérez")))
                .andExpect(jsonPath("$.dni", is("12345678A")));

        verify(clienteService, times(1)).obtenerClientePorId(1L);
    }

    @Test
    void testObtenerClientePorId_NotFound() throws Exception {
        // Given
        when(clienteService.obtenerClientePorId(999L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"));

        // When & Then
        mockMvc.perform(get("/api/clientes/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerClientePorDni() throws Exception {
        // Given
        when(clienteService.obtenerClientePorDni("12345678A")).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(get("/api/clientes/dni/12345678A")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.dni", is("12345678A")))
                .andExpect(jsonPath("$.nombre", is("Juan Pérez")));

        verify(clienteService, times(1)).obtenerClientePorDni("12345678A");
    }

    @Test
    void testObtenerClientePorDni_NotFound() throws Exception {
        // Given
        when(clienteService.obtenerClientePorDni("99999999Z"))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con DNI: 99999999Z"));

        // When & Then
        mockMvc.perform(get("/api/clientes/dni/99999999Z")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarCliente() throws Exception {
        // Given
        ClienteDTO clienteActualizado = new ClienteDTO();
        clienteActualizado.setId(1L);
        clienteActualizado.setDni("12345678A");
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setDireccion("Calle Nueva 2, Madrid");
        clienteActualizado.setEmail("juan.nuevo@email.com");
        clienteActualizado.setTelefono("600789012");

        when(clienteService.actualizarCliente(eq(1L), any(ClienteDTO.class)))
                .thenReturn(clienteActualizado);

        // When & Then
        mockMvc.perform(put("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Juan Pérez Actualizado")))
                .andExpect(jsonPath("$.email", is("juan.nuevo@email.com")));

        verify(clienteService, times(1)).actualizarCliente(eq(1L), any(ClienteDTO.class));
    }

    @Test
    void testActualizarCliente_NotFound() throws Exception {
        // Given
        when(clienteService.actualizarCliente(eq(999L), any(ClienteDTO.class)))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"));

        // When & Then
        mockMvc.perform(put("/api/clientes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarCliente() throws Exception {
        // Given
        doNothing().when(clienteService).eliminarCliente(1L);

        // When & Then
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).eliminarCliente(1L);
    }

    @Test
    void testEliminarCliente_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"))
                .when(clienteService).eliminarCliente(999L);

        // When & Then
        mockMvc.perform(delete("/api/clientes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearCliente_ValidacionDni() throws Exception {
        // Given
        ClienteDTO clienteInvalido = new ClienteDTO();
        clienteInvalido.setNombre("Juan Pérez");
        clienteInvalido.setEmail("juan@email.com");
        // DNI vacío para probar validación

        // When & Then
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest());
    }
}
