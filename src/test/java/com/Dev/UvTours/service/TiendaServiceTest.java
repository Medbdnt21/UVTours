package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.TiendaDTO;
import com.Dev.UvTours.entity.Compra;
import com.Dev.UvTours.entity.Tienda;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.CompraRepository;
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
class TiendaServiceTest {

    @Mock
    private TiendaRepository tiendaRepository;

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private TiendaService tiendaService;

    private Tienda tienda;
    private TiendaDTO tiendaDTO;

    @BeforeEach
    void setUp() {
        tienda = new Tienda();
        tienda.setId(1L);
        tienda.setDireccion("Campus Universitario 1");
        tienda.setCodigoPostal("28001");
        tienda.setTelefono("911234567");

        tiendaDTO = new TiendaDTO();
        tiendaDTO.setDireccion("Campus Universitario 1");
        tiendaDTO.setCodigoPostal("28001");
        tiendaDTO.setTelefono("911234567");
    }

    @Test
    void testListarTodasLasTiendas() {
        // Given
        when(tiendaRepository.findAll()).thenReturn(Arrays.asList(tienda));

        // When
        List<TiendaDTO> resultado = tiendaService.listarTodasLasTiendas();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Campus Universitario 1", resultado.get(0).getDireccion());
    }

    @Test
    void testObtenerTiendaPorId() {
        // Given
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));

        // When
        TiendaDTO resultado = tiendaService.obtenerTiendaPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("28001", resultado.getCodigoPostal());
    }

    @Test
    void testObtenerTiendaPorId_NoEncontrada() {
        // Given
        when(tiendaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tiendaService.obtenerTiendaPorId(999L));
    }

    @Test
    void testCrearTienda() {
        // Given
        when(tiendaRepository.save(any(Tienda.class))).thenReturn(tienda);

        // When
        TiendaDTO resultado = tiendaService.crearTienda(tiendaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("911234567", resultado.getTelefono());
        verify(tiendaRepository, times(1)).save(any(Tienda.class));
    }

    @Test
    void testActualizarTienda() {
        // Given
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(tiendaRepository.save(any(Tienda.class))).thenReturn(tienda);
        tiendaDTO.setTelefono("922345678");

        // When
        TiendaDTO resultado = tiendaService.actualizarTienda(1L, tiendaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("922345678", resultado.getTelefono());
    }

    @Test
    void testEliminarTienda() {
        // Given
        when(tiendaRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByTiendaId(1L)).thenReturn(emptyList());

        // When
        tiendaService.eliminarTienda(1L);

        // Then
        verify(tiendaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarTienda_ConCompras() {
        // Given
        Compra compra = new Compra();
        compra.setId(1L);
        when(tiendaRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findByTiendaId(1L)).thenReturn(Arrays.asList(compra));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> tiendaService.eliminarTienda(1L));
        verify(tiendaRepository, never()).deleteById(1L);
    }

    @Test
    void testEliminarTienda_NoEncontrada() {
        // Given
        when(tiendaRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tiendaService.eliminarTienda(999L));
    }
}