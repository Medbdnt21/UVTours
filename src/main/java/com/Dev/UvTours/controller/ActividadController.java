package com.Dev.UvTours.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Dev.UvTours.dto.ActividadDTO;
import com.Dev.UvTours.dto.ActividadDelDiaDTO;
import com.Dev.UvTours.service.ActividadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    @GetMapping
    public ResponseEntity<List<ActividadDTO>> listarTodas() {
        return ResponseEntity.ok(actividadService.listarTodasLasActividades());
    }

    @PostMapping
    public ResponseEntity<ActividadDTO> crearActividad(@RequestBody ActividadDTO actividadDTO) {
        return ResponseEntity.ok(actividadService.crearActividad(actividadDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadDTO> actualizarActividad(
            @PathVariable Long id,
            @RequestBody ActividadDTO actividadDTO) {
        return ResponseEntity.ok(actividadService.actualizarActividad(id, actividadDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Long id) {
        actividadService.eliminarActividad(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ActividadDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(actividadService.cambiarEstadoActividad(id, estado));
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<ActividadDelDiaDTO>> listarActividadesDelDia(
            @RequestParam(required = false) LocalDate fecha) {
        return ResponseEntity.ok(actividadService.listarActividadesDelDia(fecha));
    }
}
