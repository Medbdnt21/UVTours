package com.Dev.UvTours.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Dev.UvTours.entity.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByClienteId(Long clienteId);

    List<Compra> findByViajeId(Long viajeId);

    List<Compra> findByTiendaId(Long tiendaId);

    List<Compra> findByEmpleadoId(Long empleadoId);
}
