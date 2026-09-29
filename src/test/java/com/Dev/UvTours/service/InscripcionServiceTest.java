package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.InscripcionDTO;
import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Inscripcion;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ActividadRepository;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.InscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private InscripcionService inscripcionService;

    private Cliente cliente;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setEmail("juan@email.com");

        actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Visita al Louvre");
        actividad.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividad.setEstado(EstadoActividad.PLANIFICADA);
    }

    private Inscripcion crearInscripcion() {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(1L);
        inscripcion.setCliente(cliente);
        inscripcion.setActividad(actividad);
        return inscripcion;
    }

    @Test
    void testInscribir() {
        // Given
        Inscripcion inscripcion = crearInscripcion();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(inscripcionRepository.existsByClienteIdAndActividadId(1L, 1L)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcion);

        // When
        InscripcionDTO resultado = inscripcionService.inscribir(1L, 1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getClienteId());
        assertEquals(1L, resultado.getActividadId());
        assertNotNull(resultado.getFechaInscripcion());
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    @Test
    void testInscribir_ClienteNoEncontrado() {
        // Given
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inscripcionService.inscribir(999L, 1L));
    }

    @Test
    void testInscribir_ActividadNoEncontrada() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inscripcionService.inscribir(1L, 999L));
    }

    @Test
    void testInscribir_ActividadNoPlanificada() {
        // Given
        actividad.setEstado(EstadoActividad.EN_EJECUCION);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> inscripcionService.inscribir(1L, 1L));
    }

    @Test
    void testInscribir_YaInscrito() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(inscripcionRepository.existsByClienteIdAndActividadId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.inscribir(1L, 1L));
    }

    @Test
    void testListarPorActividad() {
        // Given
        Inscripcion inscripcion = crearInscripcion();
        when(inscripcionRepository.findByActividadId(1L)).thenReturn(Arrays.asList(inscripcion));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarPorActividad(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getClienteId());
    }

    @Test
    void testListarPorCliente() {
        // Given
        Inscripcion inscripcion = crearInscripcion();
        when(inscripcionRepository.findByClienteId(1L)).thenReturn(Arrays.asList(inscripcion));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarPorCliente(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getActividadId());
    }

    @Test
    void testAnularInscripcion() {
        // Given
        Inscripcion inscripcion = crearInscripcion();
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        doNothing().when(inscripcionRepository).delete(any(Inscripcion.class));

        // When
        inscripcionService.anularInscripcion(1L);

        // Then
        verify(inscripcionRepository, times(1)).delete(inscripcion);
    }

    @Test
    void testAnularInscripcion_NoEncontrada() {
        // Given
        when(inscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inscripcionService.anularInscripcion(999L));
    }

    @Test
    void testAnularInscripcion_ActividadNoPlanificada() {
        // Given
        actividad.setEstado(EstadoActividad.FINALIZADA);
        Inscripcion inscripcion = crearInscripcion();
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> inscripcionService.anularInscripcion(1L));
    }
}