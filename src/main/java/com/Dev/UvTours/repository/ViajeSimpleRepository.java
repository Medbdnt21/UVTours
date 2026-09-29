package com.Dev.UvTours.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.ViajeSimple;

@Repository
public interface ViajeSimpleRepository extends JpaRepository<ViajeSimple, Long> {

    List<ViajeSimple> findByDestino(String destino);
}
