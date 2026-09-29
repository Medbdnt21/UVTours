package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.CompraDTO;
import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.entity.*;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private TiendaRepository tiendaRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private CompraService compraService;

    private Cliente cliente;
    private ViajeSimple viaje;
    private Tienda tienda;
    private Empleado empleado;
    private CompraDTO compraDTO;

    @BeforeEach
    void setUp() {
        // Crear cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");

        // Crear viaje
        viaje = new ViajeSimple();
        viaje.setId(1L);
        viaje.setNombre("Viaje a París");
        viaje.setFechaInicio(LocalDate.of(2026, 8, 15));
        viaje.setFechaFin(LocalDate.of(2026, 8, 22));
        viaje.setPrecio(new BigDecimal("1200.00"));
        viaje.setDestino("París");
        viaje.setDestinoUnico(true);
        viaje.setEsLunaMiel(false);

        // Crear tienda
        tienda = new Tienda();
        tienda.setId(1L);
        tienda.setDireccion("Campus Universitario 1");
        tienda.setCodigoPostal("28001");
        tienda.setTelefono("911234567");

        // Crear empleado
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombre("Ana Martínez");
        empleado.setEmail("ana@uvtours.com");
        empleado.setDni("98765432C");
        empleado.setTienda(tienda);

        // Crear DTO de compra
        compraDTO = new CompraDTO();
        compraDTO.setClienteId(1L);
        compraDTO.setViajeId(1L);
        compraDTO.setTiendaId(1L);
        compraDTO.setEmpleadoId(1L);
        compraDTO.setCompraOnline(false);

        AcompananteDTO acompananteDTO = new AcompananteDTO();
        acompananteDTO.setNombre("María García");
        acompananteDTO.setDni("87654321B");
        compraDTO.setAcompanantes(Arrays.asList(acompananteDTO));
    }

    @Test
    void testRealizarCompraEnTienda() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1L);
            return compra;
        });

        // When
        CompraDTO resultado = compraService.realizarCompra(compraDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("1200.00"), resultado.getPrecioFinal());
        assertEquals(false, resultado.getCompraOnline());
        assertEquals(1, resultado.getAcompanantes().size());
        assertEquals("María García", resultado.getAcompanantes().get(0).getNombre());
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    void testRealizarCompraOnline() {
        // Given
        compraDTO.setCompraOnline(true);
        compraDTO.setTiendaId(null);
        compraDTO.setEmpleadoId(null);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1L);
            return compra;
        });

        // When
        CompraDTO resultado = compraService.realizarCompra(compraDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(true, resultado.getCompraOnline());
        assertNull(resultado.getTiendaId());
        assertNull(resultado.getEmpleadoId());
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    void testRealizarCompraConDescuentoLunaMiel() {
        // Given
        viaje.setEsLunaMiel(true);
        viaje.setDescuentoLunaMiel(new BigDecimal("200.00"));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1L);
            return compra;
        });

        // When
        CompraDTO resultado = compraService.realizarCompra(compraDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("1000.00"), resultado.getPrecioFinal()); // 1200 - 200
    }

    @Test
    void testRealizarCompra_ClienteNoEncontrado() {
        // Given
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        compraDTO.setClienteId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> compraService.realizarCompra(compraDTO));
    }

    @Test
    void testRealizarCompra_ViajeNoEncontrado() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(999L)).thenReturn(Optional.empty());
        compraDTO.setViajeId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> compraService.realizarCompra(compraDTO));
    }

    @Test
    void testRealizarCompra_TiendaNoEncontrada() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(999L)).thenReturn(Optional.empty());
        compraDTO.setTiendaId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> compraService.realizarCompra(compraDTO));
    }

    @Test
    void testListarCompras() {
        // Given
        Compra compra = new Compra();
        compra.setId(1L);
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);
        when(compraRepository.findAll()).thenReturn(Arrays.asList(compra));

        // When
        var resultado = compraService.listarCompras();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("1200.00"), resultado.get(0).getPrecioFinal());
    }

    @Test
    void testObtenerCompraPorId() {
        // Given
        Compra compra = new Compra();
        compra.setId(1L);
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // When
        CompraDTO resultado = compraService.obtenerCompraPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getClienteId());
        assertEquals(1L, resultado.getViajeId());
    }

    @Test
    void testObtenerCompraPorId_NoEncontrada() {
        // Given
        when(compraRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> compraService.obtenerCompraPorId(999L));
    }

    @Test
    void testRealizarCompra_CompraOnlineNullPorDefectoEnTienda() {
        // Given
        compraDTO.setCompraOnline(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1L);
            return compra;
        });

        // When
        CompraDTO resultado = compraService.realizarCompra(compraDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(false, resultado.getCompraOnline());
        assertEquals(1L, resultado.getTiendaId());
    }

    @Test
    void testRealizarCompra_EmpleadoNoEncontrado() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.findById(999L)).thenReturn(Optional.empty());
        compraDTO.setEmpleadoId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> compraService.realizarCompra(compraDTO));
    }

    @Test
    void testRealizarCompraConMultiplesAcompanantes() {
        // Given
        AcompananteDTO acompananteDTO1 = new AcompananteDTO();
        acompananteDTO1.setNombre("María García");
        acompananteDTO1.setDni("87654321B");

        AcompananteDTO acompananteDTO2 = new AcompananteDTO();
        acompananteDTO2.setNombre("Pedro López");
        acompananteDTO2.setDni("55555555D");

        compraDTO.setAcompanantes(Arrays.asList(acompananteDTO1, acompananteDTO2));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1L);
            return compra;
        });

        // When
        CompraDTO resultado = compraService.realizarCompra(compraDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.getAcompanantes().size());
    }
}
