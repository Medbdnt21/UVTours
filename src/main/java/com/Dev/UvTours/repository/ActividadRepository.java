package com.Dev.UvTours.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByEstadoAndFechaActividad(EstadoActividad estado, LocalDate fecha);

    List<Actividad> findByEstado(EstadoActividad estado);

    List<Actividad> findByFechaActividad(LocalDate fecha);
}
