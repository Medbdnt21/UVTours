package com.Dev.UvTours.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Dev.UvTours.dto.EmpleadoDTO;
import com.Dev.UvTours.dto.TiendaDTO;
import com.Dev.UvTours.entity.Tienda;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.CompraRepository;
import com.Dev.UvTours.repository.TiendaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TiendaService {

    private final TiendaRepository tiendaRepository;
    private final CompraRepository compraRepository;

    // the readonly is for optimization, it tells the persistence provider that the method will not modify any data,
    //  allowing for certain optimizations in the underlying database operations.
    @Transactional(readOnly = true)
    public List<TiendaDTO> listarTodasLasTiendas() {
        return tiendaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TiendaDTO obtenerTiendaPorId(Long id) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id: " + id));
        return convertirADTO(tienda);
    }

    @Transactional
    public TiendaDTO crearTienda(TiendaDTO tiendaDTO) {
        Tienda tienda = new Tienda();
        tienda.setDireccion(tiendaDTO.getDireccion());
        tienda.setCodigoPostal(tiendaDTO.getCodigoPostal());
        tienda.setTelefono(tiendaDTO.getTelefono());

        tienda = tiendaRepository.save(tienda);
        return convertirADTO(tienda);
    }

    @Transactional
    public TiendaDTO actualizarTienda(Long id, TiendaDTO tiendaDTO) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id: " + id));

        tienda.setDireccion(tiendaDTO.getDireccion());
        tienda.setCodigoPostal(tiendaDTO.getCodigoPostal());
        tienda.setTelefono(tiendaDTO.getTelefono());

        tienda = tiendaRepository.save(tienda);
        return convertirADTO(tienda);
    }

    @Transactional
    public void eliminarTienda(Long id) {
        if (!tiendaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tienda no encontrada con id: " + id);
        }
        if (!compraRepository.findByTiendaId(id).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar una tienda con compras asociadas");
        }
        tiendaRepository.deleteById(id);
    }

    private TiendaDTO convertirADTO(Tienda tienda) {
        TiendaDTO dto = new TiendaDTO();
        dto.setId(tienda.getId());
        dto.setDireccion(tienda.getDireccion());
        dto.setCodigoPostal(tienda.getCodigoPostal());
        dto.setTelefono(tienda.getTelefono());

        if (tienda.getEmpleados() != null && !tienda.getEmpleados().isEmpty()) {
            dto.setEmpleados(tienda.getEmpleados().stream()
                    .map(empleado -> {
                        EmpleadoDTO empleadoDTO = new EmpleadoDTO();
                        empleadoDTO.setId(empleado.getId());
                        empleadoDTO.setNombre(empleado.getNombre());
                        empleadoDTO.setEmail(empleado.getEmail());
                        empleadoDTO.setDni(empleado.getDni());
                        empleadoDTO.setTiendaId(tienda.getId());
                        return empleadoDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
