package com.Dev.UvTours.controller;

import com.Dev.UvTours.dto.TiendaDTO;
import com.Dev.UvTours.service.TiendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiendas")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    @GetMapping
    public ResponseEntity<List<TiendaDTO>> listarTodas() {
        return ResponseEntity.ok(tiendaService.listarTodasLasTiendas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiendaDTO> obtenerTienda(@PathVariable Long id) {
        return ResponseEntity.ok(tiendaService.obtenerTiendaPorId(id));
    }

    @PostMapping
    public ResponseEntity<TiendaDTO> crearTienda(@RequestBody TiendaDTO tiendaDTO) {
        return ResponseEntity.ok(tiendaService.crearTienda(tiendaDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TiendaDTO> actualizarTienda(
            @PathVariable Long id,
            @RequestBody TiendaDTO tiendaDTO) {
        return ResponseEntity.ok(tiendaService.actualizarTienda(id, tiendaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTienda(@PathVariable Long id) {
        tiendaService.eliminarTienda(id);
        return ResponseEntity.noContent().build();
    }
}