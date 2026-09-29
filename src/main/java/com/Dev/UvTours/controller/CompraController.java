package com.Dev.UvTours.controller;

import com.Dev.UvTours.dto.CompraDTO;
import com.Dev.UvTours.security.AuthUtils;
import com.Dev.UvTours.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<List<CompraDTO>> listarTodas() {
        return ResponseEntity.ok(compraService.listarCompras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtenerCompra(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerCompraPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraDTO> realizarCompra(@RequestBody CompraDTO compraDTO) {
        return ResponseEntity.ok(compraService.realizarCompra(compraDTO));
    }

    @GetMapping("/mias")
    public ResponseEntity<List<CompraDTO>> misCompras(Authentication authentication) {
        return ResponseEntity.ok(compraService.misCompras(AuthUtils.emailDe(authentication)));
    }

    @PostMapping("/mias")
    public ResponseEntity<CompraDTO> realizarCompraOnline(
            @RequestBody CompraDTO compraDTO,
            Authentication authentication) {
        return ResponseEntity.ok(
                compraService.realizarCompraAutenticada(compraDTO, AuthUtils.emailDe(authentication)));
    }
}