package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.Viaje;
import com.Dev.UvTours.entity.ViajeSimple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ViajeRepositoryTest {

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByEsLunaMielTrue() {
        // Given
        ViajeSimple viajeSimple = new ViajeSimple();
        viajeSimple.setNombre("Viaje de Luna de Miel");
        viajeSimple.setFechaInicio(LocalDate.of(2026, 8, 15));
        viajeSimple.setFechaFin(LocalDate.of(2026, 8, 22));
        viajeSimple.setPrecio(new BigDecimal("1200.00"));
        viajeSimple.setDestino("París");
        viajeSimple.setDestinoUnico(true);
        viajeSimple.setEsLunaMiel(true);
        viajeSimple.setDescuentoLunaMiel(new BigDecimal("200.00"));

        entityManager.persist(viajeSimple);
        entityManager.flush();

        // When
        List<Viaje> viajes = viajeRepository.findByEsLunaMielTrue();

        // Then
        assertNotNull(viajes);
        assertTrue(viajes.size() > 0);
        assertTrue(viajes.get(0).getEsLunaMiel());
    }

    @Test
    void testFindByFechaInicioAfter() {
        // Given
        ViajeSimple viajeSimple = new ViajeSimple();
        viajeSimple.setNombre("Viaje Futuro");
        viajeSimple.setFechaInicio(LocalDate.of(2026, 12, 15));
        viajeSimple.setFechaFin(LocalDate.of(2026, 12, 22));
        viajeSimple.setPrecio(new BigDecimal("1500.00"));
        viajeSimple.setDestino("Roma");
        viajeSimple.setDestinoUnico(true);
        viajeSimple.setEsLunaMiel(false);

        entityManager.persist(viajeSimple);
        entityManager.flush();

        // When
        List<Viaje> viajes = viajeRepository.findByFechaInicioAfter(LocalDate.of(2026, 11, 1));

        // Then
        assertNotNull(viajes);
        assertTrue(viajes.size() > 0);
        assertTrue(viajes.get(0).getFechaInicio().isAfter(LocalDate.of(2026, 11, 1)));
    }
}
