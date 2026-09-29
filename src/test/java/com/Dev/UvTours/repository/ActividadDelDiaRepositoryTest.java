package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.ActividadDelDia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ActividadDelDiaRepositoryTest {

    @Autowired
    private ActividadDelDiaRepository actividadDelDiaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByFecha() {
        // Given
        ActividadDelDia delDia = new ActividadDelDia();
        delDia.setFecha(LocalDate.of(2026, 8, 16));
        delDia.setActividadId(1L);
        delDia.setNombre("Visita al Louvre");
        delDia.setEstado(EstadoActividad.PLANIFICADA);
        entityManager.persist(delDia);

        ActividadDelDia otroDia = new ActividadDelDia();
        otroDia.setFecha(LocalDate.of(2026, 8, 17));
        otroDia.setActividadId(2L);
        otroDia.setNombre("Paseo en barco por el Sena");
        otroDia.setEstado(EstadoActividad.PLANIFICADA);
        entityManager.persist(otroDia);

        entityManager.flush();

        // When
        List<ActividadDelDia> resultado = actividadDelDiaRepository.findByFecha(LocalDate.of(2026, 8, 16));

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Visita al Louvre", resultado.get(0).getNombre());
    }

    @Test
    void testDeleteByFecha() {
        // Given
        ActividadDelDia delDia = new ActividadDelDia();
        delDia.setFecha(LocalDate.of(2026, 8, 16));
        delDia.setActividadId(1L);
        delDia.setNombre("Visita al Louvre");
        delDia.setEstado(EstadoActividad.PLANIFICADA);
        entityManager.persist(delDia);
        entityManager.flush();

        // When
        long borrados = actividadDelDiaRepository.deleteByFecha(LocalDate.of(2026, 8, 16));

        // Then
        assertEquals(1L, borrados);
        assertEquals(0, actividadDelDiaRepository.findByFecha(LocalDate.of(2026, 8, 16)).size());
    }
}