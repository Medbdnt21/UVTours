package com.Dev.UvTours.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraDTO {

    private Long id;
    private Long clienteId;
    private Long viajeId;
    private Long tiendaId;
    private Long empleadoId;
    private BigDecimal precioFinal;
    private LocalDateTime fechaCompra;
    private Boolean compraOnline;
    private List<AcompananteDTO> acompanantes;
}
