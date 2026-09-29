package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.ClienteDTO;
import com.Dev.UvTours.dto.AcompananteDTO;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Acompanante;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        Cliente cliente = new Cliente();
        cliente.setDni(clienteDTO.getDni());
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefono(clienteDTO.getTelefono());

        cliente = clienteRepository.save(cliente);
        return convertirADTO(cliente);
    }

    public ClienteDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return convertirADTO(cliente);
    }

    public ClienteDTO obtenerClientePorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dni));
        return convertirADTO(cliente);
    }

    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefono(clienteDTO.getTelefono());

        cliente = clienteRepository.save(cliente);
        return convertirADTO(cliente);
    }

    @Transactional
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setDni(cliente.getDni());
        dto.setNombre(cliente.getNombre());
        dto.setDireccion(cliente.getDireccion());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());

        if (cliente.getAcompanantes() != null) {
            dto.setAcompanantes(cliente.getAcompanantes().stream()
                    .map(this::convertirAcompananteADTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private AcompananteDTO convertirAcompananteADTO(Acompanante acompanante) {
        AcompananteDTO dto = new AcompananteDTO();
        dto.setId(acompanante.getId());
        dto.setNombre(acompanante.getNombre());
        dto.setDni(acompanante.getDni());
        return dto;
    }
}
