package com.easymype.backend.dto.product;

import com.easymype.backend.entity.EstadoStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String sku;
    private Integer stockActual;
    private Integer stockMinimo;
    private BigDecimal precioVenta;
    private BigDecimal costo;
    private EstadoStock estadoStock;
    private Long empresaId;
    private List<String> categorias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
