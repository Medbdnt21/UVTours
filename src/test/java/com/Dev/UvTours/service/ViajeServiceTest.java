package com.Dev.UvTours.service;

import java.math.BigDecimal;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Dev.UvTours.dto.ViajeDTO;
import com.Dev.UvTours.entity.Circuito;
import com.Dev.UvTours.entity.Viaje;
import com.Dev.UvTours.entity.ViajeSimple;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.CircuitoRepository;
import com.Dev.UvTours.repository.ViajeRepository;
import com.Dev.UvTours.repository.ViajeSimpleRepository;

@ExtendWith(MockitoExtension.class)
class ViajeServiceTest {

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private ViajeSimpleRepository viajeSimpleRepository;

    @Mock
    private CircuitoRepository circuitoRepository;

    @InjectMocks
    private ViajeService viajeService;

    private ViajeSimple viajeSimple;
    private Circuito circuito;
    private ViajeDTO viajeDTO;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        viajeSimple = new ViajeSimple();
        viajeSimple.setId(1L);
        viajeSimple.setNombre("Viaje a París");
        viajeSimple.setFechaInicio(LocalDate.of(2026, 8, 15));
        viajeSimple.setFechaFin(LocalDate.of(2026, 8, 22));
        viajeSimple.setPrecio(new BigDecimal("1200.00"));
        viajeSimple.setDestino("París");
        viajeSimple.setDestinoUnico(true);
        viajeSimple.setEsLunaMiel(false);

        circuito = new Circuito();
        circuito.setId(2L);
        circuito.setNombre("Circuito Europa");
        circuito.setFechaInicio(LocalDate.of(2026, 9, 1));
        circuito.setFechaFin(LocalDate.of(2026, 9, 15));
        circuito.setPrecio(new BigDecimal("2500.00"));
        circuito.setEsLunaMiel(true);
        circuito.setDescuentoLunaMiel(new BigDecimal("200.00"));

        viajeDTO = new ViajeDTO();
        viajeDTO.setId(1L);
        viajeDTO.setNombre("Viaje a París");
        viajeDTO.setFechaInicio(LocalDate.of(2026, 8, 15));
        viajeDTO.setFechaFin(LocalDate.of(2026, 8, 22));
        viajeDTO.setPrecio(new BigDecimal("1200.00"));
        viajeDTO.setTipoViaje("SIMPLE");
        viajeDTO.setDestino("París");
        viajeDTO.setEsLunaMiel(false);
    }

    @Test
    void testListarTodosLosViajes() {
        // Given
        List<Viaje> viajes = Arrays.asList(viajeSimple, circuito);
        when(viajeRepository.findAll()).thenReturn(viajes);

        // When
        List<ViajeDTO> resultado = viajeService.listarTodosLosViajes();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(viajeRepository, times(1)).findAll();
    }

    @Test
    void testCrearViajeSimple() {
        // Given
        when(viajeSimpleRepository.save(any(ViajeSimple.class))).thenReturn(viajeSimple);

        // When
        ViajeDTO resultado = viajeService.crearViaje(viajeDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Viaje a París", resultado.getNombre());
        assertEquals("SIMPLE", resultado.getTipoViaje());
        verify(viajeSimpleRepository, times(1)).save(any(ViajeSimple.class));
    }

    @Test
    void testCrearViajeCircuito() {
        // Given
        ViajeDTO circuitoDTO = new ViajeDTO();
        circuitoDTO.setNombre("Circuito Europa");
        circuitoDTO.setFechaInicio(LocalDate.of(2026, 9, 1));
        circuitoDTO.setFechaFin(LocalDate.of(2026, 9, 15));
        circuitoDTO.setPrecio(new BigDecimal("2500.00"));
        circuitoDTO.setTipoViaje("CIRCUITO");
        circuitoDTO.setEsLunaMiel(true);
        circuitoDTO.setDescuentoLunaMiel(new BigDecimal("200.00"));
        circuitoDTO.setViajesSimplesIds(Arrays.asList(1L, 2L));

        when(viajeSimpleRepository.findAllById(any())).thenReturn(Arrays.asList(viajeSimple));
        when(circuitoRepository.save(any(Circuito.class))).thenReturn(circuito);

        // When
        ViajeDTO resultado = viajeService.crearViaje(circuitoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Circuito Europa", resultado.getNombre());
        assertEquals("CIRCUITO", resultado.getTipoViaje());
        verify(circuitoRepository, times(1)).save(any(Circuito.class));
    }

    @Test
    void testActualizarViaje() {
        // Given
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viajeSimple));
        when(viajeRepository.save(any(ViajeSimple.class))).thenReturn(viajeSimple);

        viajeDTO.setNombre("Viaje a París Actualizado");

        // When
        ViajeDTO resultado = viajeService.actualizarViaje(1L, viajeDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Viaje a París Actualizado", resultado.getNombre());
        verify(viajeRepository, times(1)).save(any(ViajeSimple.class));
    }

    @Test
    void testActualizarViaje_NotFound() {
        // Given
        when(viajeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> viajeService.actualizarViaje(999L, viajeDTO));
    }

    @Test
    void testActualizarViaje_ActualizaDestino() {
        // Given
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viajeSimple));
        when(viajeRepository.save(any(ViajeSimple.class))).thenReturn(viajeSimple);

        viajeDTO.setDestino("Londres");

        // When
        ViajeDTO resultado = viajeService.actualizarViaje(1L, viajeDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Londres", resultado.getDestino());
    }

    @Test
    void testActualizarViaje_CircuitoRecalculaPrecio() {
        // Given
        Circuito circuitoAModificar = new Circuito();
        circuitoAModificar.setId(2L);
        circuitoAModificar.setNombre("Circuito Europa");
        circuitoAModificar.setEsLunaMiel(false);

        ViajeSimple paris = new ViajeSimple();
        paris.setId(1L);
        paris.setPrecio(new BigDecimal("1200.00"));
        ViajeSimple roma = new ViajeSimple();
        roma.setId(2L);
        roma.setPrecio(new BigDecimal("800.00"));

        when(viajeRepository.findById(2L)).thenReturn(Optional.of(circuitoAModificar));
        when(viajeSimpleRepository.findAllById(any())).thenReturn(Arrays.asList(paris, roma));
        when(viajeRepository.save(any(Circuito.class))).thenReturn(circuitoAModificar);

        ViajeDTO circuitoDTO = new ViajeDTO();
        circuitoDTO.setNombre("Circuito Europa");
        circuitoDTO.setTipoViaje("CIRCUITO");
        circuitoDTO.setEsLunaMiel(false);
        circuitoDTO.setViajesSimplesIds(Arrays.asList(1L, 2L));

        // When
        ViajeDTO resultado = viajeService.actualizarViaje(2L, circuitoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("2000.00"), resultado.getPrecio());
    }

    @Test
    void testActualizarViaje_LimpiaDescuentoAlQuitarLunaMiel() {
        // Given
        ViajeSimple paris = new ViajeSimple();
        paris.setId(1L);
        paris.setPrecio(new BigDecimal("1200.00"));

        when(viajeRepository.findById(2L)).thenReturn(Optional.of(circuito));
        when(viajeSimpleRepository.findAllById(any())).thenReturn(Arrays.asList(paris));
        when(viajeRepository.save(any(Circuito.class))).thenReturn(circuito);

        ViajeDTO circuitoDTO = new ViajeDTO();
        circuitoDTO.setNombre("Circuito Europa");
        circuitoDTO.setTipoViaje("CIRCUITO");
        circuitoDTO.setEsLunaMiel(false);
        circuitoDTO.setViajesSimplesIds(Arrays.asList(1L));

        // When
        ViajeDTO resultado = viajeService.actualizarViaje(2L, circuitoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.getDescuentoLunaMiel());
        assertEquals(new BigDecimal("1200.00"), resultado.getPrecio());
    }

    @Test
    void testEliminarViaje() {
        // Given
        when(viajeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(viajeRepository).deleteById(1L);

        // When
        viajeService.eliminarViaje(1L);

        // Then
        verify(viajeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarViaje_NotFound() {
        // Given
        when(viajeRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> viajeService.eliminarViaje(999L));
    }

    @Test
    void testActualizarDescuentoLunaMiel() {
        // Given
        when(viajeRepository.findById(2L)).thenReturn(Optional.of(circuito));
        when(viajeRepository.save(any(Circuito.class))).thenReturn(circuito);

        BigDecimal nuevoDescuento = new BigDecimal("300.00");

        // When
        ViajeDTO resultado = viajeService.actualizarDescuentoLunaMiel(2L, nuevoDescuento);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("300.00"), resultado.getDescuentoLunaMiel());
    }

    @Test
    void testActualizarDescuentoLunaMiel_NoLunaMiel() {
        // Given
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viajeSimple));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> viajeService.actualizarDescuentoLunaMiel(1L, new BigDecimal("100.00")));
    }
}
