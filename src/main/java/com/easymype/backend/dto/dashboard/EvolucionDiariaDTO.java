package com.easymype.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EvolucionDiariaDTO {
    private LocalDate fecha;
    private BigDecimal ingresos;
    private Long cantidadVentas;
}
