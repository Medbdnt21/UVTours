package com.Dev.UvTours.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Circuito;

@Repository
public interface CircuitoRepository extends JpaRepository<Circuito, Long> {
}
