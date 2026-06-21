package com.easymype.backend.service;

import com.easymype.backend.dto.sale.SaleItemRequestDTO;
import com.easymype.backend.dto.sale.SaleRequestDTO;
import com.easymype.backend.dto.sale.SaleResponseDTO;
import com.easymype.backend.entity.*;
import com.easymype.backend.event.SaleCreatedEvent;
import com.easymype.backend.exception.InsufficientStockException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.SaleMapper;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final SaleMapper saleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SaleResponseDTO registrarVenta(SaleRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        Venta venta = Venta.builder()
                .empresa(usuario.getEmpresa())
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .detalles(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequestDTO item : request.getItems()) {
            Producto producto = productoRepository.findByIdAndEmpresaId(item.getProductoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto con id=" + item.getProductoId() + " no encontrado en su empresa"));

            if (producto.getStockActual() < item.getCantidad()) {
                throw new InsufficientStockException(
                        producto.getNombre(), item.getCantidad(), producto.getStockActual());
            }

            producto.setStockActual(producto.getStockActual() - item.getCantidad());
            producto.recalcularEstadoStock();
            productoRepository.save(producto);

            BigDecimal subtotal = producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .subtotal(subtotal)
                    .build();

            venta.getDetalles().add(detalle);
            total = total.add(subtotal);
        }

        venta.setTotal(total);
        Venta ventaGuardada = ventaRepository.save(venta);

        // Publicar evento para generación de alertas DESPUÉS del commit
        eventPublisher.publishEvent(new SaleCreatedEvent(this, ventaGuardada));

        return saleMapper.toResponse(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> findAll(Usuario usuario) {
        return saleMapper.toResponseList(
                ventaRepository.findByEmpresaId(usuario.getEmpresa().getId())
        );
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long id, Usuario usuario) {
        Venta venta = ventaRepository.findByIdAndEmpresaId(id, usuario.getEmpresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Venta con id=" + id + " no encontrada"));
        return saleMapper.toResponse(venta);
    }
}
