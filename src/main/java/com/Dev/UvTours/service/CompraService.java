package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.CompraDTO;
import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.entity.*;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ClienteRepository clienteRepository;
    private final ViajeRepository viajeRepository;
    private final TiendaRepository tiendaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<CompraDTO> listarCompras() {
        return compraRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<CompraDTO> misCompras(String email) {
        Cliente cliente = clienteDe(email);
        return compraRepository.findByClienteId(cliente.getId()).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompraDTO realizarCompraAutenticada(CompraDTO compraDTO, String email) {
        Cliente cliente = clienteDe(email);
        compraDTO.setClienteId(cliente.getId());
        compraDTO.setCompraOnline(true);
        return realizarCompra(compraDTO);
    }

    private Cliente clienteDe(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        if (usuario.getCliente() == null) {
            throw new IllegalArgumentException(
                    "El usuario no está vinculado a un cliente. Vincula tu cuenta para operar online");
        }
        return usuario.getCliente();
    }

    public CompraDTO obtenerCompraPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        return convertirADTO(compra);
    }

    @Transactional
    public CompraDTO realizarCompra(CompraDTO compraDTO) {
        Cliente cliente = clienteRepository.findById(compraDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Viaje viaje = viajeRepository.findById(compraDTO.getViajeId())
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado"));

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setViaje(viaje);
        compra.setFechaCompra(LocalDateTime.now());
        boolean compraOnline = Boolean.TRUE.equals(compraDTO.getCompraOnline());
        compra.setCompraOnline(compraOnline);

        // Calcular precio final
        BigDecimal precioFinal = viaje.getPrecio();
        if (viaje.getEsLunaMiel() && viaje.getDescuentoLunaMiel() != null) {
            precioFinal = precioFinal.subtract(viaje.getDescuentoLunaMiel());
        }
        compra.setPrecioFinal(precioFinal);

        // Si la compra es en tienda, asociar tienda y empleado
        if (!compraOnline) {
            Tienda tienda = tiendaRepository.findById(compraDTO.getTiendaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada"));
            compra.setTienda(tienda);

            if (compraDTO.getEmpleadoId() != null) {
                Empleado empleado = empleadoRepository.findById(compraDTO.getEmpleadoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));
                compra.setEmpleado(empleado);
            }
        }

        // Agregar acompañantes
        if (compraDTO.getAcompanantes() != null) {
            final Compra compraActual = compra;
            compraDTO.getAcompanantes().forEach(acompananteDTO -> {
                Acompanante acompanante = new Acompanante();
                acompanante.setNombre(acompananteDTO.getNombre());
                acompanante.setDni(acompananteDTO.getDni());
                acompanante.setCliente(cliente);
                acompanante.setCompra(compraActual);
                compraActual.getAcompanantes().add(acompanante);
            });
        }

        compra = compraRepository.save(compra);
        return convertirADTO(compra);
    }

    private CompraDTO convertirADTO(Compra compra) {
        CompraDTO dto = new CompraDTO();
        dto.setId(compra.getId());
        dto.setClienteId(compra.getCliente().getId());
        dto.setViajeId(compra.getViaje().getId());
        dto.setPrecioFinal(compra.getPrecioFinal());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setCompraOnline(compra.getCompraOnline());

        if (compra.getTienda() != null) {
            dto.setTiendaId(compra.getTienda().getId());
        }
        if (compra.getEmpleado() != null) {
            dto.setEmpleadoId(compra.getEmpleado().getId());
        }

        if (compra.getAcompanantes() != null) {
            dto.setAcompanantes(compra.getAcompanantes().stream()
                    .map(acompanante -> {
                        AcompananteDTO acompananteDTO = new AcompananteDTO();
                        acompananteDTO.setId(acompanante.getId());
                        acompananteDTO.setNombre(acompanante.getNombre());
                        acompananteDTO.setDni(acompanante.getDni());
                        return acompananteDTO;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        }

        return dto;
    }
}
