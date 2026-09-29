package com.Dev.UvTours.service;

import com.Dev.UvTours.dto.ViajeDTO;
import com.Dev.UvTours.entity.*;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final ViajeSimpleRepository viajeSimpleRepository;
    private final CircuitoRepository circuitoRepository;
    private final ActividadRepository actividadRepository;

    public List<ViajeDTO> listarTodosLosViajes() {
        return viajeRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ViajeDTO crearViaje(ViajeDTO viajeDTO) {
        if ("SIMPLE".equals(viajeDTO.getTipoViaje())) {
            ViajeSimple viajeSimple = new ViajeSimple();
            viajeSimple.setNombre(viajeDTO.getNombre());
            viajeSimple.setFechaInicio(viajeDTO.getFechaInicio());
            viajeSimple.setFechaFin(viajeDTO.getFechaFin());
            viajeSimple.setPrecio(viajeDTO.getPrecio());
            viajeSimple.setEsLunaMiel(viajeDTO.getEsLunaMiel());
            viajeSimple.setDestino(viajeDTO.getDestino());
            viajeSimple.setDestinoUnico(true);

            if (viajeDTO.getEsLunaMiel()) {
                viajeSimple.setDescuentoLunaMiel(viajeDTO.getDescuentoLunaMiel());
            }

            viajeSimple = viajeSimpleRepository.save(viajeSimple);
            return convertirADTO(viajeSimple);

        } else if ("CIRCUITO".equals(viajeDTO.getTipoViaje())) {
            Circuito circuito = new Circuito();
            circuito.setNombre(viajeDTO.getNombre());
            circuito.setFechaInicio(viajeDTO.getFechaInicio());
            circuito.setFechaFin(viajeDTO.getFechaFin());
            circuito.setPrecio(viajeDTO.getPrecio());
            circuito.setEsLunaMiel(viajeDTO.getEsLunaMiel());

            if (viajeDTO.getViajesSimplesIds() != null) {
                List<ViajeSimple> viajesSimples = viajeSimpleRepository.findAllById(viajeDTO.getViajesSimplesIds());
                circuito.setViajesSimples(viajesSimples);
                BigDecimal costeTotal = circuito.calcularCosteTotal();
                circuito.setPrecio(costeTotal);
            }

            if (viajeDTO.getEsLunaMiel()) {
                circuito.setDescuentoLunaMiel(viajeDTO.getDescuentoLunaMiel());
            }

            circuito = circuitoRepository.save(circuito);
            return convertirADTO(circuito);
        }

        throw new IllegalArgumentException("Tipo de viaje no válido");
    }

    @Transactional
    public ViajeDTO actualizarViaje(Long id, ViajeDTO viajeDTO) {
        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con id: " + id));

        viaje.setNombre(viajeDTO.getNombre());
        viaje.setFechaInicio(viajeDTO.getFechaInicio());
        viaje.setFechaFin(viajeDTO.getFechaFin());
        viaje.setPrecio(viajeDTO.getPrecio());
        viaje.setEsLunaMiel(viajeDTO.getEsLunaMiel());

        if (Boolean.TRUE.equals(viajeDTO.getEsLunaMiel())) {
            viaje.setDescuentoLunaMiel(viajeDTO.getDescuentoLunaMiel());
        } else {
            viaje.setDescuentoLunaMiel(BigDecimal.ZERO);
        }

        if (viaje instanceof ViajeSimple viajeSimple) {
            viajeSimple.setDestino(viajeDTO.getDestino());
        } else if (viaje instanceof Circuito circuito) {
            if (viajeDTO.getViajesSimplesIds() != null) {
                List<ViajeSimple> viajesSimples = viajeSimpleRepository.findAllById(viajeDTO.getViajesSimplesIds());
                circuito.setViajesSimples(viajesSimples);
            }
            viaje.setPrecio(circuito.calcularCosteTotal());
        }

        viaje = viajeRepository.save(viaje);
        return convertirADTO(viaje);
    }

    @Transactional
    public void eliminarViaje(Long id) {
        if (!viajeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Viaje no encontrado con id: " + id);
        }
        viajeRepository.deleteById(id);
    }

    @Transactional
    public ViajeDTO actualizarDescuentoLunaMiel(Long id, BigDecimal descuento) {
        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con id: " + id));

        if (!viaje.getEsLunaMiel()) {
            throw new IllegalArgumentException("El viaje no es de tipo Luna de Miel");
        }

        viaje.setDescuentoLunaMiel(descuento);
        viaje = viajeRepository.save(viaje);
        return convertirADTO(viaje);
    }

    private ViajeDTO convertirADTO(Viaje viaje) {
        ViajeDTO dto = new ViajeDTO();
        dto.setId(viaje.getId());
        dto.setNombre(viaje.getNombre());
        dto.setFechaInicio(viaje.getFechaInicio());
        dto.setFechaFin(viaje.getFechaFin());
        dto.setPrecio(viaje.getPrecio());
        dto.setEsLunaMiel(viaje.getEsLunaMiel());
        dto.setDescuentoLunaMiel(viaje.getDescuentoLunaMiel());

        if (viaje instanceof ViajeSimple) {
            dto.setTipoViaje("SIMPLE");
            dto.setDestino(((ViajeSimple) viaje).getDestino());
        } else if (viaje instanceof Circuito) {
            dto.setTipoViaje("CIRCUITO");
            dto.setViajesSimplesIds(((Circuito) viaje).getViajesSimples().stream()
                    .map(ViajeSimple::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
