package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.EmpleadoDTO;
import com.Dev.UvTours.entity.Compra;
import com.Dev.UvTours.entity.Empleado;
import com.Dev.UvTours.entity.Tienda;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.CompraRepository;
import com.Dev.UvTours.repository.EmpleadoRepository;
import com.Dev.UvTours.repository.TiendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private TiendaRepository tiendaRepository;

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private EmpleadoService empleadoService;

    private Tienda tienda;
    private Empleado empleado;
    private EmpleadoDTO empleadoDTO;

    @BeforeEach
    void setUp() {
        tienda = new Tienda();
        tienda.setId(1L);
        tienda.setDireccion("Campus Universitario 1");
        tienda.setCodigoPostal("28001");
        tienda.setTelefono("911234567");

        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombre("Ana Martínez");
        empleado.setEmail("ana@uvtours.com");
        empleado.setDni("98765432C");
        empleado.setTienda(tienda);

        empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setNombre("Ana Martínez");
        empleadoDTO.setEmail("ana@uvtours.com");
        empleadoDTO.setDni("98765432C");
        empleadoDTO.setTiendaId(1L);
    }

    @Test
    void testListarTodosLosEmpleados() {
        // Given
        when(empleadoRepository.findAll()).thenReturn(Arrays.asList(empleado));

        // When
        List<EmpleadoDTO> resultado = empleadoService.listarTodosLosEmpleados();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ana Martínez", resultado.get(0).getNombre());
        assertEquals(1L, resultado.get(0).getTiendaId());
    }

    @Test
    void testObtenerEmpleadoPorId() {
        // Given
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));

        // When
        EmpleadoDTO resultado = empleadoService.obtenerEmpleadoPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("ana@uvtours.com", resultado.getEmail());
    }

    @Test
    void testObtenerEmpleadoPorId_NoEncontrado() {
        // Given
        when(empleadoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> empleadoService.obtenerEmpleadoPorId(999L));
    }

    @Test
    void testCrearEmpleado() {
        // Given
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleado);

        // When
        EmpleadoDTO resultado = empleadoService.crearEmpleado(empleadoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getTiendaId());
        assertEquals("98765432C", resultado.getDni());
        verify(empleadoRepository, times(1)).save(any(Empleado.class));
    }

    @Test
    void testCrearEmpleado_TiendaNoEncontrada() {
        // Given
        when(tiendaRepository.findById(999L)).thenReturn(Optional.empty());
        empleadoDTO.setTiendaId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> empleadoService.crearEmpleado(empleadoDTO));
    }

    @Test
    void testActualizarEmpleado() {
        // Given
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleado);
        empleadoDTO.setNombre("Ana M. Sánchez");

        // When
        EmpleadoDTO resultado = empleadoService.actualizarEmpleado(1L, empleadoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Ana M. Sánchez", resultado.getNombre());
    }

    @Test
    void testEliminarEmpleado() {
        // Given
        when(empleadoRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByEmpleadoId(1L)).thenReturn(emptyList());

        // When
        empleadoService.eliminarEmpleado(1L);

        // Then
        verify(empleadoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarEmpleado_ConCompras() {
        // Given
        Compra compra = new Compra();
        compra.setId(1L);
        when(empleadoRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByEmpleadoId(1L)).thenReturn(Arrays.asList(compra));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> empleadoService.eliminarEmpleado(1L));
        verify(empleadoRepository, never()).deleteById(1L);
    }

    @Test
    void testEliminarEmpleado_NoEncontrado() {
        // Given
        when(empleadoRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> empleadoService.eliminarEmpleado(999L));
    }
}