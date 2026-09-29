package com.Dev.UvTours.controller;

import com.Dev.UvTours.dto.ViajeDTO;
import com.Dev.UvTours.service.ViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private final ViajeService viajeService;

    @GetMapping
    public ResponseEntity<List<ViajeDTO>> listarTodos() {
        return ResponseEntity.ok(viajeService.listarTodosLosViajes());
    }

    @PostMapping
    public ResponseEntity<ViajeDTO> crearViaje(@RequestBody ViajeDTO viajeDTO) {
        return ResponseEntity.ok(viajeService.crearViaje(viajeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViajeDTO> actualizarViaje(
            @PathVariable Long id,
            @RequestBody ViajeDTO viajeDTO) {
        return ResponseEntity.ok(viajeService.actualizarViaje(id, viajeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarViaje(@PathVariable Long id) {
        viajeService.eliminarViaje(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/descuento-luna-miel")
    public ResponseEntity<ViajeDTO> actualizarDescuentoLunaMiel(
            @PathVariable Long id,
            @RequestParam BigDecimal descuento) {
        return ResponseEntity.ok(viajeService.actualizarDescuentoLunaMiel(id, descuento));
    }
}
