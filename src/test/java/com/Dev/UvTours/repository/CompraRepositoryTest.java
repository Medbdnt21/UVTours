package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CompraRepositoryTest {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByClienteId() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");
        entityManager.persist(cliente);

        ViajeSimple viaje = new ViajeSimple();
        viaje.setNombre("Viaje a París");
        viaje.setFechaInicio(LocalDate.of(2026, 8, 15));
        viaje.setFechaFin(LocalDate.of(2026, 8, 22));
        viaje.setPrecio(new BigDecimal("1200.00"));
        viaje.setDestino("París");
        viaje.setDestinoUnico(true);
        entityManager.persist(viaje);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);
        entityManager.persist(compra);

        entityManager.flush();

        // When
        List<Compra> compras = compraRepository.findByClienteId(cliente.getId());

        // Then
        assertNotNull(compras);
        assertEquals(1, compras.size());
        assertEquals(viaje.getNombre(), compras.get(0).getViaje().getNombre());
    }

    @Test
    void testFindByViajeId() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");
        entityManager.persist(cliente);

        ViajeSimple viaje = new ViajeSimple();
        viaje.setNombre("Viaje a París");
        viaje.setFechaInicio(LocalDate.of(2026, 8, 15));
        viaje.setFechaFin(LocalDate.of(2026, 8, 22));
        viaje.setPrecio(new BigDecimal("1200.00"));
        viaje.setDestino("París");
        viaje.setDestinoUnico(true);
        entityManager.persist(viaje);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);
        entityManager.persist(compra);

        entityManager.flush();

        // When
        List<Compra> compras = compraRepository.findByViajeId(viaje.getId());

        // Then
        assertNotNull(compras);
        assertEquals(1, compras.size());
        assertEquals(cliente.getNombre(), compras.get(0).getCliente().getNombre());
    }

    @Test
    void testCompraConAcompanantes() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");
        entityManager.persist(cliente);

        ViajeSimple viaje = new ViajeSimple();
        viaje.setNombre("Viaje a París");
        viaje.setFechaInicio(LocalDate.of(2026, 8, 15));
        viaje.setFechaFin(LocalDate.of(2026, 8, 22));
        viaje.setPrecio(new BigDecimal("1200.00"));
        viaje.setDestino("París");
        viaje.setDestinoUnico(true);
        entityManager.persist(viaje);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);
        entityManager.persist(compra);

        Acompanante acompanante1 = new Acompanante();
        acompanante1.setNombre("María García");
        acompanante1.setDni("87654321B");
        acompanante1.setCliente(cliente);
        acompanante1.setCompra(compra);
        compra.getAcompanantes().add(acompanante1);

        Acompanante acompanante2 = new Acompanante();
        acompanante2.setNombre("Pedro López");
        acompanante2.setDni("55555555D");
        acompanante2.setCliente(cliente);
        acompanante2.setCompra(compra);
        compra.getAcompanantes().add(acompanante2);

        entityManager.flush();
        entityManager.clear();

        // When
        Compra encontrada = entityManager.find(Compra.class, compra.getId());

        // Then
        assertNotNull(encontrada);
        assertEquals(2, encontrada.getAcompanantes().size());
        assertTrue(encontrada.getAcompanantes().stream()
                .anyMatch(a -> a.getNombre().equals("María García")));
        assertTrue(encontrada.getAcompanantes().stream()
                .anyMatch(a -> a.getNombre().equals("Pedro López")));
    }
}
