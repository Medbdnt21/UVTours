package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Acompanante;
import com.Dev.UvTours.entity.Compra;
import com.Dev.UvTours.entity.ViajeSimple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByDni() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");

        entityManager.persist(cliente);
        entityManager.flush();

        // When
        Optional<Cliente> encontrado = clienteRepository.findByDni("12345678A");

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals("Juan Pérez", encontrado.get().getNombre());
        assertEquals("juan@email.com", encontrado.get().getEmail());
    }

    @Test
    void testFindByDni_NotFound() {
        // When
        Optional<Cliente> encontrado = clienteRepository.findByDni("99999999Z");

        // Then
        assertFalse(encontrado.isPresent());
    }

    @Test
    void testFindByEmail() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");

        entityManager.persist(cliente);
        entityManager.flush();

        // When
        Optional<Cliente> encontrado = clienteRepository.findByEmail("juan@email.com");

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals("Juan Pérez", encontrado.get().getNombre());
        assertEquals("12345678A", encontrado.get().getDni());
    }

    @Test
    void testClienteConAcompanantes() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");

        ViajeSimple viaje = new ViajeSimple();
        viaje.setNombre("Viaje a París");
        viaje.setFechaInicio(LocalDate.of(2026, 8, 15));
        viaje.setFechaFin(LocalDate.of(2026, 8, 22));
        viaje.setPrecio(new BigDecimal("1200.00"));
        viaje.setDestino("París");
        viaje.setDestinoUnico(true);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setPrecioFinal(new BigDecimal("1200.00"));
        compra.setCompraOnline(false);

        Acompanante acompanante = new Acompanante();
        acompanante.setNombre("María García");
        acompanante.setDni("87654321B");
        acompanante.setCliente(cliente);
        acompanante.setCompra(compra);

        compra.getAcompanantes().add(acompanante);

        entityManager.persist(cliente);
        entityManager.persist(viaje);
        entityManager.persist(compra);
        entityManager.flush();
        entityManager.clear();

        // When
        Cliente encontrado = entityManager.find(Cliente.class, cliente.getId());

        // Then
        assertNotNull(encontrado);
        assertEquals(1, encontrado.getAcompanantes().size());
        assertEquals("María García", encontrado.getAcompanantes().get(0).getNombre());
    }
}
