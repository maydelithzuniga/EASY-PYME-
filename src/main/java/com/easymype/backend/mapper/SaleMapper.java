package com.easymype.backend.mapper;

import com.easymype.backend.dto.sale.SaleItemResponseDTO;
import com.easymype.backend.dto.sale.SaleResponseDTO;
import com.easymype.backend.entity.DetalleVenta;
import com.easymype.backend.entity.Venta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaleMapper {

    public SaleResponseDTO toResponse(Venta venta) {
        List<SaleItemResponseDTO> detalles = venta.getDetalles().stream()
                .map(this::toItemResponse)
                .toList();

        return SaleResponseDTO.builder()
                .id(venta.getId())
                .empresaId(venta.getEmpresa().getId())
                .usuarioUsername(venta.getUsuario().getUsername())
                .fecha(venta.getFecha())
                .total(venta.getTotal())
                .detalles(detalles)
                .createdAt(venta.getCreatedAt())
                .build();
    }

    private SaleItemResponseDTO toItemResponse(DetalleVenta detalle) {
        return SaleItemResponseDTO.builder()
                .id(detalle.getId())
                .productoId(detalle.getProducto().getId())
                .productoNombre(detalle.getProducto().getNombre())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }

    public List<SaleResponseDTO> toResponseList(List<Venta> ventas) {
        return ventas.stream().map(this::toResponse).toList();
    }
}
