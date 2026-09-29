package com.Dev.UvTours.service;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.dto.ClienteDTO;
import com.Dev.UvTours.entity.Acompanante;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private ClienteDTO clienteDTO;
    private Acompanante acompanante;

    @BeforeEach
    void setUp() {
        // Crear acompañante
        acompanante = new Acompanante();
        acompanante.setId(1L);
        acompanante.setNombre("María García");
        acompanante.setDni("87654321B");

        // Crear cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");
        cliente.setAcompanantes(Arrays.asList(acompanante));

        // Crear DTO
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
    void testCrearCliente() {
        // Given
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        assertEquals("12345678A", resultado.getDni());
        assertEquals("juan@email.com", resultado.getEmail());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testObtenerClientePorId() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // When
        ClienteDTO resultado = clienteService.obtenerClientePorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        assertEquals(1, resultado.getAcompanantes().size());
        assertEquals("María García", resultado.getAcompanantes().get(0).getNombre());
    }

    @Test
    void testObtenerClientePorId_NotFound() {
        // Given
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clienteService.obtenerClientePorId(999L));
    }

    @Test
    void testObtenerClientePorDni() {
        // Given
        when(clienteRepository.findByDni("12345678A")).thenReturn(Optional.of(cliente));

        // When
        ClienteDTO resultado = clienteService.obtenerClientePorDni("12345678A");

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        assertEquals("12345678A", resultado.getDni());
    }

    @Test
    void testObtenerClientePorDni_NotFound() {
        // Given
        when(clienteRepository.findByDni("99999999Z")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clienteService.obtenerClientePorDni("99999999Z"));
    }

    @Test
    void testActualizarCliente() {
        // Given
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1L);
        clienteActualizado.setDni("12345678A");
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setDireccion("Calle Nueva 2, Madrid");
        clienteActualizado.setEmail("juan.nuevo@email.com");
        clienteActualizado.setTelefono("600789012");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        ClienteDTO clienteActualizadoDTO = new ClienteDTO();
        clienteActualizadoDTO.setNombre("Juan Pérez Actualizado");
        clienteActualizadoDTO.setDireccion("Calle Nueva 2, Madrid");
        clienteActualizadoDTO.setEmail("juan.nuevo@email.com");
        clienteActualizadoDTO.setTelefono("600789012");

        // When
        ClienteDTO resultado = clienteService.actualizarCliente(1L, clienteActualizadoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals("juan.nuevo@email.com", resultado.getEmail());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testActualizarCliente_NotFound() {
        // Given
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clienteService.actualizarCliente(999L, clienteDTO));
    }

    @Test
    void testEliminarCliente() {
        // Given
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        // When
        clienteService.eliminarCliente(1L);

        // Then
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarCliente_NotFound() {
        // Given
        when(clienteRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clienteService.eliminarCliente(999L));
    }
}
