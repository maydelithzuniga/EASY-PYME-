package com.easymype.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder

public class ProductoInmovilizadoDTO {

    private Long productoId;
    private String nombre;
    private String sku;
    private Integer stockActual;
    private BigDecimal precioUnitario;
    private BigDecimal costoUnitario;
    private BigDecimal valorInmovilizado;             // stock * costo
    private Long diasSinVenta;
}
