package com.Dev.UvTours.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Inscripcion;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByActividadId(Long actividadId);

    List<Inscripcion> findByClienteId(Long clienteId);

    boolean existsByClienteIdAndActividadId(Long clienteId, Long actividadId);
}