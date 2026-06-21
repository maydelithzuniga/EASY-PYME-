package com.easymype.backend.mapper;

import com.easymype.backend.dto.product.ProductResponseDTO;
import com.easymype.backend.entity.Producto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponse(Producto producto) {
        List<String> categorias = producto.getCategorias().stream()
                .map(c -> c.getNombre())
                .toList();

        return ProductResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .sku(producto.getSku())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .precioVenta(producto.getPrecioVenta())
                .costo(producto.getCosto())
                .estadoStock(producto.getEstadoStock())
                .empresaId(producto.getEmpresa().getId())
                .categorias(categorias)
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .build();
    }

    public List<ProductResponseDTO> toResponseList(List<Producto> productos) {
        return productos.stream().map(this::toResponse).toList();
    }
}
