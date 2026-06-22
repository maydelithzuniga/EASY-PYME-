package com.easymype.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RentabilidadProductoDTO {
    private Long productoId;
    private String nombre;
    private String sku;
    private BigDecimal totalVentas;
    private BigDecimal costoTotal;
    private BigDecimal utilidadBruta;
    private BigDecimal margenBrutoPorcentual;
    private Long unidadesVendidas;
}
