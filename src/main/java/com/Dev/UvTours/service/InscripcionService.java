package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.InscripcionDTO;
import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Inscripcion;
import com.Dev.UvTours.entity.Usuario;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ActividadRepository;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.InscripcionRepository;
import com.Dev.UvTours.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public InscripcionDTO inscribir(Long clienteId, Long actividadId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clienteId));

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id: " + actividadId));

        if (actividad.getEstado() != EstadoActividad.PLANIFICADA) {
            throw new IllegalStateException("No se pueden registrar inscripciones en una actividad no planificada");
        }

        if (inscripcionRepository.existsByClienteIdAndActividadId(clienteId, actividadId)) {
            throw new IllegalArgumentException("El cliente ya está inscrito en la actividad");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setActividad(actividad);
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        inscripcion = inscripcionRepository.save(inscripcion);
        return convertirADTO(inscripcion);
    }

    public List<InscripcionDTO> listarPorActividad(Long actividadId) {
        return inscripcionRepository.findByActividadId(actividadId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<InscripcionDTO> listarPorCliente(Long clienteId) {
        return inscripcionRepository.findByClienteId(clienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<InscripcionDTO> misInscripciones(String email) {
        Cliente cliente = clienteDe(email);
        return inscripcionRepository.findByClienteId(cliente.getId()).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InscripcionDTO inscribirMia(Long actividadId, String email) {
        Cliente cliente = clienteDe(email);
        return inscribir(cliente.getId(), actividadId);
    }

    @Transactional
    public void anularInscripcionMia(Long id, String email) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada con id: " + id));
        if (!inscripcion.getCliente().getId().equals(clienteDe(email).getId())) {
            throw new AccessDeniedException("No puedes anular una inscripción que no es tuya");
        }
        anularInscripcion(id);
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

    @Transactional
    public void anularInscripcion(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada con id: " + id));

        if (inscripcion.getActividad().getEstado() != EstadoActividad.PLANIFICADA) {
            throw new IllegalStateException("No se puede anular una inscripcion en una actividad no planificada");
        }

        inscripcionRepository.delete(inscripcion);
    }

    private InscripcionDTO convertirADTO(Inscripcion inscripcion) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(inscripcion.getId());
        dto.setClienteId(inscripcion.getCliente().getId());
        dto.setActividadId(inscripcion.getActividad().getId());
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        return dto;
    }
}