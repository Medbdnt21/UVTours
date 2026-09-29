package com.Dev.UvTours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Tienda;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Long> {
}
