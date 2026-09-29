package com.Dev.UvTours.controller;

import com.Dev.UvTours.dto.EmpleadoDTO;
import com.Dev.UvTours.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listarTodos() {
        return ResponseEntity.ok(empleadoService.listarTodosLosEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> obtenerEmpleado(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.obtenerEmpleadoPorId(id));
    }

    @PostMapping
    public ResponseEntity<EmpleadoDTO> crearEmpleado(@RequestBody EmpleadoDTO empleadoDTO) {
        return ResponseEntity.ok(empleadoService.crearEmpleado(empleadoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> actualizarEmpleado(
            @PathVariable Long id,
            @RequestBody EmpleadoDTO empleadoDTO) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, empleadoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}