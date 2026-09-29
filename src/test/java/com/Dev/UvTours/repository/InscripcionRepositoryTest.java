package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Inscripcion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InscripcionRepositoryTest {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByActividadId() {
        // Given
        Cliente cliente = persistirCliente();
        Actividad actividad = persistirActividad();
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setActividad(actividad);
        entityManager.persist(inscripcion);
        entityManager.flush();

        // When
        List<Inscripcion> inscripciones = inscripcionRepository.findByActividadId(actividad.getId());

        // Then
        assertNotNull(inscripciones);
        assertEquals(1, inscripciones.size());
        assertEquals(cliente.getNombre(), inscripciones.get(0).getCliente().getNombre());
    }

    @Test
    void testFindByClienteId() {
        // Given
        Cliente cliente = persistirCliente();
        Actividad actividad = persistirActividad();
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setActividad(actividad);
        entityManager.persist(inscripcion);
        entityManager.flush();

        // When
        List<Inscripcion> inscripciones = inscripcionRepository.findByClienteId(cliente.getId());

        // Then
        assertNotNull(inscripciones);
        assertEquals(1, inscripciones.size());
        assertEquals(actividad.getNombre(), inscripciones.get(0).getActividad().getNombre());
    }

    @Test
    void testExistsByClienteIdAndActividadId() {
        // Given
        Cliente cliente = persistirCliente();
        Actividad actividad = persistirActividad();
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setActividad(actividad);
        entityManager.persist(inscripcion);
        entityManager.flush();

        // When & Then
        assertTrue(inscripcionRepository.existsByClienteIdAndActividadId(cliente.getId(), actividad.getId()));
        assertFalse(inscripcionRepository.existsByClienteIdAndActividadId(999L, actividad.getId()));
    }

    @Test
    void testNoPermiteDuplicados() {
        // Given
        Cliente cliente = persistirCliente();
        Actividad actividad = persistirActividad();

        Inscripcion inscripcion1 = new Inscripcion();
        inscripcion1.setCliente(cliente);
        inscripcion1.setActividad(actividad);
        entityManager.persist(inscripcion1);
        entityManager.flush();

        Inscripcion inscripcion2 = new Inscripcion();
        inscripcion2.setCliente(cliente);
        inscripcion2.setActividad(actividad);

        // When & Then
        assertThrows(Exception.class, () -> {
            entityManager.persist(inscripcion2);
            entityManager.flush();
        });
    }

    private Cliente persistirCliente() {
        Cliente cliente = new Cliente();
        cliente.setDni("12345678A");
        cliente.setNombre("Juan Pérez");
        cliente.setDireccion("Calle Mayor 1, Madrid");
        cliente.setEmail("juan@email.com");
        cliente.setTelefono("600123456");
        entityManager.persist(cliente);
        return cliente;
    }

    private Actividad persistirActividad() {
        Actividad actividad = new Actividad();
        actividad.setNombre("Visita al Louvre");
        actividad.setFechaActividad(LocalDate.of(2026, 8, 16));
        actividad.setEstado(EstadoActividad.PLANIFICADA);
        entityManager.persist(actividad);
        return actividad;
    }
}