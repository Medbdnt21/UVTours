package com.Dev.UvTours.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Viaje;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    List<Viaje> findByEsLunaMielTrue();

    List<Viaje> findByFechaInicioAfter(LocalDate date);
}
