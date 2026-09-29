package com.Dev.UvTours.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Dev.UvTours.dto.ActividadDTO;
import com.Dev.UvTours.dto.ActividadDelDiaDTO;
import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.ActividadDelDia;
import com.Dev.UvTours.repository.ActividadDelDiaRepository;
import com.Dev.UvTours.repository.ActividadRepository;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private ActividadDelDiaRepository actividadDelDiaRepository;

    @InjectMocks
    private ActividadService actividadService;

    private Actividad actividad;
    private ActividadDTO actividadDTO;

    @BeforeEach
    void setUp() {
        actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Visita al Louvre");
        actividad.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividad.setEstado(EstadoActividad.PLANIFICADA);

        actividadDTO = new ActividadDTO();
        actividadDTO.setId(1L);
        actividadDTO.setNombre("Visita al Louvre");
        actividadDTO.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividadDTO.setEstado("PLANIFICADA");
    }

    @Test
    void testListarTodasLasActividades() {
        // Given
        List<Actividad> actividades = Arrays.asList(actividad);
        when(actividadRepository.findAll()).thenReturn(actividades);

        // When
        List<ActividadDTO> resultado = actividadService.listarTodasLasActividades();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Visita al Louvre", resultado.get(0).getNombre());
    }

    @Test
    void testCrearActividad() {
        // Given
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);

        // When
        ActividadDTO resultado = actividadService.crearActividad(actividadDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Visita al Louvre", resultado.getNombre());
        assertEquals("PLANIFICADA", resultado.getEstado());
        verify(actividadRepository, times(1)).save(any(Actividad.class));
    }

    @Test
    void testActualizarActividad() {
        // Given
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);

        actividadDTO.setNombre("Visita al Museo del Louvre");

        // When
        ActividadDTO resultado = actividadService.actualizarActividad(1L, actividadDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Visita al Museo del Louvre", resultado.getNombre());
    }

    @Test
    void testActualizarActividad_EnEjecucion() {
        // Given
        actividad.setEstado(EstadoActividad.EN_EJECUCION);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> actividadService.actualizarActividad(1L, actividadDTO));
    }

    @Test
    void testCambiarEstadoActividad_AEjecucion() {
        // Given
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);

        // When
        ActividadDTO resultado = actividadService.cambiarEstadoActividad(1L, "EN_EJECUCION");

        // Then
        assertNotNull(resultado);
        assertEquals("EN_EJECUCION", resultado.getEstado());
    }

    @Test
    void testCambiarEstadoActividad_Cancelar() {
        // Given
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);

        // When
        ActividadDTO resultado = actividadService.cambiarEstadoActividad(1L, "CANCELADA");

        // Then
        assertNotNull(resultado);
        assertEquals("CANCELADA", resultado.getEstado());
        assertNotNull(actividad.getFechaCancelacion());
    }

    @Test
    void testCambiarEstadoActividad_EstadoInvalido() {
        // Given
        actividad.setEstado(EstadoActividad.FINALIZADA);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> actividadService.cambiarEstadoActividad(1L, "EN_EJECUCION"));
    }

    @Test
    void testGenerarListaActividadesDelDia() {
        // Given
        LocalDate hoy = LocalDate.now();
        when(actividadRepository.findByEstadoAndFechaActividad(eq(EstadoActividad.PLANIFICADA), eq(hoy)))
                .thenReturn(Arrays.asList(actividad));
        when(actividadDelDiaRepository.deleteByFecha(hoy)).thenReturn(0L);

        // When
        actividadService.generarListaActividadesDelDia();

        // Then
        verify(actividadDelDiaRepository, times(1)).deleteByFecha(hoy);
        verify(actividadDelDiaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGenerarListaActividadesDelDia_SinActividades() {
        // Given
        LocalDate hoy = LocalDate.now();
        when(actividadRepository.findByEstadoAndFechaActividad(eq(EstadoActividad.PLANIFICADA), eq(hoy)))
                .thenReturn(Arrays.asList());
        when(actividadDelDiaRepository.deleteByFecha(hoy)).thenReturn(0L);

        // When
        actividadService.generarListaActividadesDelDia();

        // Then
        verify(actividadDelDiaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testListarActividadesDelDia() {
        // Given
        ActividadDelDia actividadDelDia = new ActividadDelDia();
        actividadDelDia.setId(1L);
        actividadDelDia.setFecha(LocalDate.of(2026, 8, 16));
        actividadDelDia.setActividadId(1L);
        actividadDelDia.setNombre("Visita al Louvre");
        actividadDelDia.setEstado(EstadoActividad.PLANIFICADA);
        when(actividadDelDiaRepository.findByFecha(LocalDate.of(2026, 8, 16)))
                .thenReturn(Arrays.asList(actividadDelDia));

        // When
        List<ActividadDelDiaDTO> resultado = actividadService.listarActividadesDelDia(LocalDate.of(2026, 8, 16));

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Visita al Louvre", resultado.get(0).getNombre());
        assertEquals("PLANIFICADA", resultado.get(0).getEstado());
    }
}
