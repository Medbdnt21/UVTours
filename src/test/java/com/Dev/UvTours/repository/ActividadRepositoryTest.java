package com.Dev.UvTours.repository;

import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ActividadRepositoryTest {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Actividad actividad1;
    private Actividad actividad2;
    private Actividad actividad3;
    private Actividad actividad4;

    @BeforeEach
    void setUp() {
        // Actividad 1: Planificada para hoy
        actividad1 = new Actividad();
        actividad1.setNombre("Visita al Louvre");
        actividad1.setFechaActividad(LocalDate.now());
        actividad1.setEstado(EstadoActividad.PLANIFICADA);

        // Actividad 2: Planificada para mañana
        actividad2 = new Actividad();
        actividad2.setNombre("Paseo en barco por el Sena");
        actividad2.setFechaActividad(LocalDate.now().plusDays(1));
        actividad2.setEstado(EstadoActividad.PLANIFICADA);

        // Actividad 3: En ejecución
        actividad3 = new Actividad();
        actividad3.setNombre("Tour gastronómico");
        actividad3.setFechaActividad(LocalDate.now());
        actividad3.setEstado(EstadoActividad.EN_EJECUCION);

        // Actividad 4: Finalizada
        actividad4 = new Actividad();
        actividad4.setNombre("Visita a la Torre Eiffel");
        actividad4.setFechaActividad(LocalDate.now().minusDays(1));
        actividad4.setEstado(EstadoActividad.FINALIZADA);
        actividad4.setFechaFinalizacion(LocalDate.now().minusDays(1));

        // Persistir todas las actividades
        entityManager.persist(actividad1);
        entityManager.persist(actividad2);
        entityManager.persist(actividad3);
        entityManager.persist(actividad4);
        entityManager.flush();
    }

    @Test
    void testFindByEstadoAndFechaActividad() {
        // When: Buscar actividades planificadas para hoy
        List<Actividad> actividades = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now());

        // Then
        assertNotNull(actividades);
        assertEquals(1, actividades.size());
        assertEquals("Visita al Louvre", actividades.get(0).getNombre());
        assertEquals(EstadoActividad.PLANIFICADA, actividades.get(0).getEstado());
        assertEquals(LocalDate.now(), actividades.get(0).getFechaActividad());
    }

    @Test
    void testFindByEstadoAndFechaActividad_NoResults() {
        // When: Buscar actividades planificadas para ayer (no debería haber)
        List<Actividad> actividades = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now().minusDays(1));

        // Then
        assertNotNull(actividades);
        assertEquals(0, actividades.size());
    }

    @Test
    void testFindByEstado() {
        // When: Buscar todas las actividades planificadas
        List<Actividad> planificadas = actividadRepository.findByEstado(EstadoActividad.PLANIFICADA);

        // Then
        assertNotNull(planificadas);
        assertEquals(2, planificadas.size());
        assertTrue(planificadas.stream().allMatch(a -> a.getEstado() == EstadoActividad.PLANIFICADA));
        assertTrue(planificadas.stream().anyMatch(a -> a.getNombre().equals("Visita al Louvre")));
        assertTrue(planificadas.stream().anyMatch(a -> a.getNombre().equals("Paseo en barco por el Sena")));

        // When: Buscar todas las actividades en ejecución
        List<Actividad> enEjecucion = actividadRepository.findByEstado(EstadoActividad.EN_EJECUCION);

        // Then
        assertNotNull(enEjecucion);
        assertEquals(1, enEjecucion.size());
        assertEquals("Tour gastronómico", enEjecucion.get(0).getNombre());
        assertEquals(EstadoActividad.EN_EJECUCION, enEjecucion.get(0).getEstado());

        // When: Buscar todas las actividades finalizadas
        List<Actividad> finalizadas = actividadRepository.findByEstado(EstadoActividad.FINALIZADA);

        // Then
        assertNotNull(finalizadas);
        assertEquals(1, finalizadas.size());
        assertEquals("Visita a la Torre Eiffel", finalizadas.get(0).getNombre());
        assertEquals(EstadoActividad.FINALIZADA, finalizadas.get(0).getEstado());
    }

    @Test
    void testFindByFechaActividad() {
        // When: Buscar actividades por fecha (hoy)
        List<Actividad> actividadesHoy = actividadRepository.findByFechaActividad(LocalDate.now());

        // Then
        assertNotNull(actividadesHoy);
        assertEquals(2, actividadesHoy.size()); // Actividad1 y Actividad3 son hoy
        assertTrue(actividadesHoy.stream().anyMatch(a -> a.getNombre().equals("Visita al Louvre")));
        assertTrue(actividadesHoy.stream().anyMatch(a -> a.getNombre().equals("Tour gastronómico")));

        // When: Buscar actividades por fecha (mañana)
        List<Actividad> actividadesManana = actividadRepository.findByFechaActividad(LocalDate.now().plusDays(1));

        // Then
        assertNotNull(actividadesManana);
        assertEquals(1, actividadesManana.size());
        assertEquals("Paseo en barco por el Sena", actividadesManana.get(0).getNombre());
    }

    @Test
    void testFindByEstadoAndFechaActividad_ConActividadesCanceladas() {
        // Given: Crear una actividad cancelada
        Actividad actividadCancelada = new Actividad();
        actividadCancelada.setNombre("Tour cancelado");
        actividadCancelada.setFechaActividad(LocalDate.now());
        actividadCancelada.setEstado(EstadoActividad.CANCELADA);
        actividadCancelada.setFechaCancelacion(LocalDate.now());
        entityManager.persist(actividadCancelada);
        entityManager.flush();

        // When: Buscar actividades planificadas para hoy
        List<Actividad> actividades = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now());

        // Then: La actividad cancelada NO debe aparecer
        assertNotNull(actividades);
        assertEquals(1, actividades.size());
        assertEquals("Visita al Louvre", actividades.get(0).getNombre());
        assertFalse(actividades.stream().anyMatch(a -> a.getNombre().equals("Tour cancelado")));
    }

    @Test
    void testFindByEstado_FiltraCorrectamenteTodosLosEstados() {
        // Given: Crear una actividad cancelada
        Actividad actividadCancelada = new Actividad();
        actividadCancelada.setNombre("Tour cancelado");
        actividadCancelada.setFechaActividad(LocalDate.now().plusDays(2));
        actividadCancelada.setEstado(EstadoActividad.CANCELADA);
        actividadCancelada.setFechaCancelacion(LocalDate.now());
        entityManager.persist(actividadCancelada);
        entityManager.flush();

        // When: Buscar por cada estado
        List<Actividad> planificadas = actividadRepository.findByEstado(EstadoActividad.PLANIFICADA);
        List<Actividad> enEjecucion = actividadRepository.findByEstado(EstadoActividad.EN_EJECUCION);
        List<Actividad> finalizadas = actividadRepository.findByEstado(EstadoActividad.FINALIZADA);
        List<Actividad> canceladas = actividadRepository.findByEstado(EstadoActividad.CANCELADA);

        // Then: Verificar que cada lista contiene el número correcto
        assertEquals(2, planificadas.size());
        assertEquals(1, enEjecucion.size());
        assertEquals(1, finalizadas.size());
        assertEquals(1, canceladas.size());

        // Verificar que no hay solapamiento
        assertTrue(planificadas.stream().noneMatch(a -> a.getEstado() == EstadoActividad.CANCELADA));
        assertTrue(canceladas.stream().allMatch(a -> a.getEstado() == EstadoActividad.CANCELADA));
    }

    @Test
    void testPersistirActividadConFechas() {
        // Given: Crear una nueva actividad
        Actividad nuevaActividad = new Actividad();
        nuevaActividad.setNombre("Nueva actividad");
        nuevaActividad.setFechaActividad(LocalDate.now().plusDays(5));
        nuevaActividad.setEstado(EstadoActividad.PLANIFICADA);

        // When: Persistir
        Actividad guardada = entityManager.persistAndFlush(nuevaActividad);

        // Then: Verificar que se guardó correctamente
        assertNotNull(guardada.getId());
        assertEquals("Nueva actividad", guardada.getNombre());
        assertEquals(LocalDate.now().plusDays(5), guardada.getFechaActividad());
        assertEquals(EstadoActividad.PLANIFICADA, guardada.getEstado());
        assertNull(guardada.getFechaCancelacion());
        assertNull(guardada.getFechaFinalizacion());

        // When: Cambiar estado a EN_EJECUCION
        guardada.setEstado(EstadoActividad.EN_EJECUCION);
        entityManager.persist(guardada);
        entityManager.flush();

        // Then: Verificar el cambio
        Actividad actualizada = entityManager.find(Actividad.class, guardada.getId());
        assertEquals(EstadoActividad.EN_EJECUCION, actualizada.getEstado());

        // When: Cambiar estado a FINALIZADA
        actualizada.setEstado(EstadoActividad.FINALIZADA);
        actualizada.setFechaFinalizacion(LocalDate.now());
        entityManager.persist(actualizada);
        entityManager.flush();

        // Then: Verificar el cambio
        Actividad finalizada = entityManager.find(Actividad.class, guardada.getId());
        assertEquals(EstadoActividad.FINALIZADA, finalizada.getEstado());
        assertNotNull(finalizada.getFechaFinalizacion());
    }

    @Test
    void testFindByEstadoAndFechaActividad_ParaScheduler() {
        // Este test simula lo que haría el scheduler a las 9:00 AM

        // Given: Actividades para diferentes días
        // Ya tenemos actividad1 para hoy y actividad2 para mañana
        // Crear otra actividad para hoy pero en estado EN_EJECUCION
        Actividad actividadHoyEjecucion = new Actividad();
        actividadHoyEjecucion.setNombre("Tour de hoy en ejecución");
        actividadHoyEjecucion.setFechaActividad(LocalDate.now());
        actividadHoyEjecucion.setEstado(EstadoActividad.EN_EJECUCION);
        entityManager.persist(actividadHoyEjecucion);
        entityManager.flush();

        // When: Obtener actividades planificadas para hoy (lo que haría el scheduler)
        List<Actividad> actividadesHoyPlanificadas = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now());

        // Then: Solo debe devolver actividad1 (planificada para hoy)
        assertNotNull(actividadesHoyPlanificadas);
        assertEquals(1, actividadesHoyPlanificadas.size());
        assertEquals("Visita al Louvre", actividadesHoyPlanificadas.get(0).getNombre());

        // La actividad en ejecución NO debe aparecer
        assertFalse(actividadesHoyPlanificadas.stream()
                .anyMatch(a -> a.getNombre().equals("Tour de hoy en ejecución")));
    }

    @Test
    void testActualizarEstadoActividad() {
        // Given: Obtener una actividad planificada
        Actividad actividad = entityManager.find(Actividad.class, actividad1.getId());
        assertEquals(EstadoActividad.PLANIFICADA, actividad.getEstado());

        // When: Cambiar a EN_EJECUCION
        actividad.setEstado(EstadoActividad.EN_EJECUCION);
        entityManager.persist(actividad);
        entityManager.flush();

        // Then: Verificar cambio
        Actividad actualizada = entityManager.find(Actividad.class, actividad1.getId());
        assertEquals(EstadoActividad.EN_EJECUCION, actualizada.getEstado());

        // When: Cambiar a FINALIZADA
        actualizada.setEstado(EstadoActividad.FINALIZADA);
        actualizada.setFechaFinalizacion(LocalDate.now());
        entityManager.persist(actualizada);
        entityManager.flush();

        // Then: Verificar cambio
        Actividad finalizada = entityManager.find(Actividad.class, actividad1.getId());
        assertEquals(EstadoActividad.FINALIZADA, finalizada.getEstado());
        assertNotNull(finalizada.getFechaFinalizacion());

        // When: Cambiar a CANCELADA (desde PLANIFICADA)
        Actividad actividad2Entity = entityManager.find(Actividad.class, actividad2.getId());
        actividad2Entity.setEstado(EstadoActividad.CANCELADA);
        actividad2Entity.setFechaCancelacion(LocalDate.now());
        entityManager.persist(actividad2Entity);
        entityManager.flush();

        // Then: Verificar cambio
        Actividad cancelada = entityManager.find(Actividad.class, actividad2.getId());
        assertEquals(EstadoActividad.CANCELADA, cancelada.getEstado());
        assertNotNull(cancelada.getFechaCancelacion());
    }

    @Test
    void testFindByEstado_EstadosMultiples() {
        // When: Buscar actividades que NO están finalizadas ni canceladas
        List<Actividad> activas = actividadRepository.findByEstado(EstadoActividad.PLANIFICADA);
        activas.addAll(actividadRepository.findByEstado(EstadoActividad.EN_EJECUCION));

        // Then
        assertNotNull(activas);
        assertEquals(3, activas.size()); // 2 planificadas + 1 en ejecución
        assertTrue(activas.stream().allMatch(a
                -> a.getEstado() == EstadoActividad.PLANIFICADA
                || a.getEstado() == EstadoActividad.EN_EJECUCION
        ));
        assertFalse(activas.stream().anyMatch(a
                -> a.getEstado() == EstadoActividad.FINALIZADA
                || a.getEstado() == EstadoActividad.CANCELADA
        ));
    }

    @Test
    void testEliminarActividad() {
        // Given: Una actividad existente
        assertNotNull(entityManager.find(Actividad.class, actividad1.getId()));

        // When: Eliminar
        actividadRepository.deleteById(actividad1.getId());
        entityManager.flush();

        // Then: Ya no existe
        Actividad eliminada = entityManager.find(Actividad.class, actividad1.getId());
        assertNull(eliminada);

        // Verificar que las otras actividades siguen existiendo
        assertNotNull(entityManager.find(Actividad.class, actividad2.getId()));
        assertNotNull(entityManager.find(Actividad.class, actividad3.getId()));
        assertNotNull(entityManager.find(Actividad.class, actividad4.getId()));
    }

    @Test
    void testFindByFechaActividad_RangoDeFechas() {
        // Given: Actividades en diferentes fechas
        // actividad1: hoy, actividad2: mañana, actividad3: hoy, actividad4: ayer

        // When: Buscar actividades desde ayer hasta mañana
        List<Actividad> actividadesRango = actividadRepository.findByFechaActividad(LocalDate.now().minusDays(1));
        actividadesRango.addAll(actividadRepository.findByFechaActividad(LocalDate.now()));
        actividadesRango.addAll(actividadRepository.findByFechaActividad(LocalDate.now().plusDays(1)));

        // Then
        assertNotNull(actividadesRango);
        assertEquals(4, actividadesRango.size()); // Todas las actividades
    }

    @Test
    void testFindByEstadoAndFechaActividad_ConFechaFutura() {
        // When: Buscar actividades planificadas para una fecha futura (mañana)
        List<Actividad> actividadesFuturas = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now().plusDays(1));

        // Then
        assertNotNull(actividadesFuturas);
        assertEquals(1, actividadesFuturas.size());
        assertEquals("Paseo en barco por el Sena", actividadesFuturas.get(0).getNombre());
    }

    @Test
    void testFindByEstadoAndFechaActividad_ConFechaPasada() {
        // When: Buscar actividades planificadas para una fecha pasada (ayer)
        List<Actividad> actividadesPasadas = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, LocalDate.now().minusDays(1));

        // Then: No debería haber actividades planificadas para ayer
        assertNotNull(actividadesPasadas);
        assertEquals(0, actividadesPasadas.size());
    }
}
