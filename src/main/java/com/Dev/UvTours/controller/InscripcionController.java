package com.Dev.UvTours.controller;

import com.Dev.UvTours.dto.InscripcionDTO;
import com.Dev.UvTours.security.AuthUtils;
import com.Dev.UvTours.service.InscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @PostMapping
    public ResponseEntity<InscripcionDTO> inscribir(@RequestBody InscripcionDTO inscripcionDTO) {
        return ResponseEntity.ok(
                inscripcionService.inscribir(inscripcionDTO.getClienteId(), inscripcionDTO.getActividadId()));
    }

    @GetMapping
    public ResponseEntity<List<InscripcionDTO>> listar(
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) Long clienteId) {
        if (actividadId != null) {
            return ResponseEntity.ok(inscripcionService.listarPorActividad(actividadId));
        }
        if (clienteId != null) {
            return ResponseEntity.ok(inscripcionService.listarPorCliente(clienteId));
        }
        throw new IllegalArgumentException("Debe indicar actividadId o clienteId");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        inscripcionService.anularInscripcion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mias")
    public ResponseEntity<List<InscripcionDTO>> misInscripciones(Authentication authentication) {
        return ResponseEntity.ok(inscripcionService.misInscripciones(AuthUtils.emailDe(authentication)));
    }

    @PostMapping("/mias")
    public ResponseEntity<InscripcionDTO> inscribirMia(
            @RequestBody InscripcionDTO inscripcionDTO,
            Authentication authentication) {
        return ResponseEntity.ok(
                inscripcionService.inscribirMia(inscripcionDTO.getActividadId(), AuthUtils.emailDe(authentication)));
    }

    @DeleteMapping("/mias/{id}")
    public ResponseEntity<Void> anularMia(@PathVariable Long id, Authentication authentication) {
        inscripcionService.anularInscripcionMia(id, AuthUtils.emailDe(authentication));
        return ResponseEntity.noContent().build();
    }
}