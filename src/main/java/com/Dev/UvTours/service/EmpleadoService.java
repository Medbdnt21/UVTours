package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.EmpleadoDTO;
import com.Dev.UvTours.entity.Empleado;
import com.Dev.UvTours.entity.Tienda;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.CompraRepository;
import com.Dev.UvTours.repository.EmpleadoRepository;
import com.Dev.UvTours.repository.TiendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final TiendaRepository tiendaRepository;
    private final CompraRepository compraRepository;

    public List<EmpleadoDTO> listarTodosLosEmpleados() {
        return empleadoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public EmpleadoDTO obtenerEmpleadoPorId(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + id));
        return convertirADTO(empleado);
    }

    @Transactional
    public EmpleadoDTO crearEmpleado(EmpleadoDTO empleadoDTO) {
        Tienda tienda = tiendaRepository.findById(empleadoDTO.getTiendaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id: " + empleadoDTO.getTiendaId()));

        Empleado empleado = new Empleado();
        empleado.setNombre(empleadoDTO.getNombre());
        empleado.setEmail(empleadoDTO.getEmail());
        empleado.setDni(empleadoDTO.getDni());
        empleado.setTienda(tienda);

        empleado = empleadoRepository.save(empleado);
        return convertirADTO(empleado);
    }

    @Transactional
    public EmpleadoDTO actualizarEmpleado(Long id, EmpleadoDTO empleadoDTO) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + id));

        Tienda tienda = tiendaRepository.findById(empleadoDTO.getTiendaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id: " + empleadoDTO.getTiendaId()));

        empleado.setNombre(empleadoDTO.getNombre());
        empleado.setEmail(empleadoDTO.getEmail());
        empleado.setDni(empleadoDTO.getDni());
        empleado.setTienda(tienda);

        empleado = empleadoRepository.save(empleado);
        return convertirADTO(empleado);
    }

    @Transactional
    public void eliminarEmpleado(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empleado no encontrado con id: " + id);
        }
        if (!compraRepository.findByEmpleadoId(id).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un empleado con compras asociadas");
        }
        empleadoRepository.deleteById(id);
    }

    private EmpleadoDTO convertirADTO(Empleado empleado) {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setId(empleado.getId());
        dto.setNombre(empleado.getNombre());
        dto.setEmail(empleado.getEmail());
        dto.setDni(empleado.getDni());
        if (empleado.getTienda() != null) {
            dto.setTiendaId(empleado.getTienda().getId());
        }
        return dto;
    }
}