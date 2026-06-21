package com.easymype.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoTopVentaDTO {

    private Long productoId;
    private String nombre;
    private String sku;
    private Long cantidadVendida;
    private BigDecimal montoTotalVendido;
}
