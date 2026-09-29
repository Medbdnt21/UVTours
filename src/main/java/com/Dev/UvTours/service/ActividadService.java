package com.Dev.UvTours.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Dev.UvTours.dto.ActividadDTO;
import com.Dev.UvTours.dto.ActividadDelDiaDTO;
import com.Dev.UvTours.entity.Actividad;
import com.Dev.UvTours.entity.Actividad.EstadoActividad;
import com.Dev.UvTours.entity.ActividadDelDia;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ActividadDelDiaRepository;
import com.Dev.UvTours.repository.ActividadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ActividadDelDiaRepository actividadDelDiaRepository;

    public List<ActividadDTO> listarTodasLasActividades() {
        return actividadRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActividadDTO crearActividad(ActividadDTO actividadDTO) {
        Actividad actividad = new Actividad();
        actividad.setNombre(actividadDTO.getNombre());
        actividad.setFechaActividad(actividadDTO.getFechaActividad());
        actividad.setEstado(EstadoActividad.PLANIFICADA);

        actividad = actividadRepository.save(actividad);
        return convertirADTO(actividad);
    }

    @Transactional
    public ActividadDTO actualizarActividad(Long id, ActividadDTO actividadDTO) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id: " + id));

        if (actividad.getEstado() == EstadoActividad.EN_EJECUCION
                || actividad.getEstado() == EstadoActividad.FINALIZADA) {
            throw new IllegalStateException("No se puede modificar una actividad en ejecución o finalizada");
        }

        actividad.setNombre(actividadDTO.getNombre());
        actividad.setFechaActividad(actividadDTO.getFechaActividad());

        actividad = actividadRepository.save(actividad);
        return convertirADTO(actividad);
    }

    @Transactional
    public void eliminarActividad(Long id) {
        if (!actividadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Actividad no encontrada con id: " + id);
        }
        actividadRepository.deleteById(id);
    }

    @Transactional
    public ActividadDTO cambiarEstadoActividad(Long id, String nuevoEstado) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id: " + id));

        EstadoActividad estado = EstadoActividad.valueOf(nuevoEstado);

        switch (estado) {
            case EN_EJECUCION:
                if (actividad.getEstado() != EstadoActividad.PLANIFICADA) {
                    throw new IllegalStateException("Solo se puede iniciar una actividad en estado PLANIFICADA");
                }
                actividad.setEstado(EstadoActividad.EN_EJECUCION);
                break;

            case FINALIZADA:
                if (actividad.getEstado() != EstadoActividad.EN_EJECUCION) {
                    throw new IllegalStateException("Solo se puede finalizar una actividad en estado EN_EJECUCION");
                }
                actividad.setEstado(EstadoActividad.FINALIZADA);
                actividad.setFechaFinalizacion(LocalDate.now());
                break;

            case CANCELADA:
                if (actividad.getEstado() != EstadoActividad.PLANIFICADA) {
                    throw new IllegalStateException("Solo se puede cancelar una actividad en estado PLANIFICADA");
                }
                actividad.setEstado(EstadoActividad.CANCELADA);
                actividad.setFechaCancelacion(LocalDate.now());
                break;

            default:
                throw new IllegalArgumentException("Estado no válido");
        }

        actividad = actividadRepository.save(actividad);
        return convertirADTO(actividad);
    }

    @Scheduled(cron = "0 0 9 * * ?") // Todos los días a las 9:00
    @Transactional
    public void generarListaActividadesDelDia() {
        LocalDate hoy = LocalDate.now();
        List<Actividad> actividadesHoy = actividadRepository.findByEstadoAndFechaActividad(
                EstadoActividad.PLANIFICADA, hoy);

        actividadDelDiaRepository.deleteByFecha(hoy);
        List<ActividadDelDia> lista = actividadesHoy.stream()
                .map(actividad -> {
                    ActividadDelDia registro = new ActividadDelDia();
                    registro.setFecha(hoy);
                    registro.setActividadId(actividad.getId());
                    registro.setNombre(actividad.getNombre());
                    registro.setEstado(actividad.getEstado());
                    return registro;
                })
                .collect(Collectors.toList());
        actividadDelDiaRepository.saveAll(lista);
    }

    public List<ActividadDelDiaDTO> listarActividadesDelDia(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return actividadDelDiaRepository.findByFecha(dia).stream()
                .map(this::convertirActividadDelDiaADTO)
                .collect(Collectors.toList());
    }

    private ActividadDTO convertirADTO(Actividad actividad) {
        ActividadDTO dto = new ActividadDTO();
        dto.setId(actividad.getId());
        dto.setNombre(actividad.getNombre());
        dto.setFechaActividad(actividad.getFechaActividad());
        dto.setEstado(actividad.getEstado().name());
        return dto;
    }

    private ActividadDelDiaDTO convertirActividadDelDiaADTO(ActividadDelDia actividadDelDia) {
        ActividadDelDiaDTO dto = new ActividadDelDiaDTO();
        dto.setId(actividadDelDia.getId());
        dto.setFecha(actividadDelDia.getFecha());
        dto.setActividadId(actividadDelDia.getActividadId());
        dto.setNombre(actividadDelDia.getNombre());
        dto.setEstado(actividadDelDia.getEstado().name());
        return dto;
    }
}
