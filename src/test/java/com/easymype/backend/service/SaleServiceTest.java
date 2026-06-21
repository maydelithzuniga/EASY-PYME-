package com.easymype.backend.service;

import com.easymype.backend.dto.sale.SaleItemRequestDTO;
import com.easymype.backend.dto.sale.SaleRequestDTO;
import com.easymype.backend.dto.sale.SaleResponseDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.entity.Venta;
import com.easymype.backend.event.SaleCreatedEvent;
import com.easymype.backend.exception.InsufficientStockException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.SaleMapper;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SaleService saleService;

    @Test
    @DisplayName("shouldRegisterSaleWhenProductsHaveEnoughStock")
    void shouldRegisterSaleWhenProductsHaveEnoughStock() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(10);
        SaleRequestDTO request = crearSaleRequest(1L, 2);

        SaleResponseDTO response = SaleResponseDTO.builder()
                .id(1L)
                .empresaId(1L)
                .usuarioUsername("luis")
                .total(new BigDecimal("200.00"))
                .build();

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            venta.setId(1L);
            return venta;
        });
        when(saleMapper.toResponse(any(Venta.class))).thenReturn(response);

        SaleResponseDTO result = saleService.registrarVenta(request, usuario);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualByComparingTo("200.00");
        verify(productoRepository).save(producto);
        verify(ventaRepository).save(any(Venta.class));
        verify(eventPublisher).publishEvent(any(SaleCreatedEvent.class));
    }

    @Test
    @DisplayName("shouldDecreaseProductStockWhenSaleIsRegistered")
    void shouldDecreaseProductStockWhenSaleIsRegistered() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(10);
        SaleRequestDTO request = crearSaleRequest(1L, 3);

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(Venta.class))).thenReturn(SaleResponseDTO.builder().build());

        saleService.registrarVenta(request, usuario);

        assertThat(producto.getStockActual()).isEqualTo(7);
        assertThat(producto.getEstadoStock()).isEqualTo(EstadoStock.DISPONIBLE);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("shouldRecalculateStockStatusWhenSaleLeavesLowStock")
    void shouldRecalculateStockStatusWhenSaleLeavesLowStock() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(6);
        producto.setStockMinimo(5);

        SaleRequestDTO request = crearSaleRequest(1L, 2);

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(Venta.class))).thenReturn(SaleResponseDTO.builder().build());

        saleService.registrarVenta(request, usuario);

        assertThat(producto.getStockActual()).isEqualTo(4);
        assertThat(producto.getEstadoStock()).isEqualTo(EstadoStock.BAJO);
    }

    @Test
    @DisplayName("shouldThrowInsufficientStockExceptionWhenStockIsNotEnough")
    void shouldThrowInsufficientStockExceptionWhenStockIsNotEnough() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(1);
        SaleRequestDTO request = crearSaleRequest(1L, 5);

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));

        assertThrows(InsufficientStockException.class, () -> saleService.registrarVenta(request, usuario));

        verify(ventaRepository, never()).save(any(Venta.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
        Usuario usuario = crearUsuario();
        SaleRequestDTO request = crearSaleRequest(99L, 1);

        when(productoRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> saleService.registrarVenta(request, usuario));

        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("shouldCalculateTotalWhenSaleHasMultipleItems")
    void shouldCalculateTotalWhenSaleHasMultipleItems() {
        Usuario usuario = crearUsuario();

        Producto producto1 = crearProducto(10);
        producto1.setId(1L);
        producto1.setPrecioVenta(new BigDecimal("100.00"));

        Producto producto2 = crearProducto(10);
        producto2.setId(2L);
        producto2.setPrecioVenta(new BigDecimal("50.00"));

        SaleRequestDTO request = new SaleRequestDTO();
        request.setItems(List.of(
                crearItem(1L, 2),
                crearItem(2L, 3)
        ));

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findByIdAndEmpresaId(2L, 1L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(Venta.class))).thenReturn(SaleResponseDTO.builder().build());

        saleService.registrarVenta(request, usuario);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository).save(ventaCaptor.capture());

        Venta venta = ventaCaptor.getValue();

        assertThat(venta.getTotal()).isEqualByComparingTo("350.00");
        assertThat(venta.getDetalles()).hasSize(2);
    }

    @Test
    @DisplayName("shouldFindAllSalesByEmpresa")
    void shouldFindAllSalesByEmpresa() {
        Usuario usuario = crearUsuario();
        Venta venta = Venta.builder()
                .id(1L)
                .empresa(usuario.getEmpresa())
                .usuario(usuario)
                .build();

        SaleResponseDTO response = SaleResponseDTO.builder()
                .id(1L)
                .empresaId(1L)
                .build();

        when(ventaRepository.findByEmpresaId(1L)).thenReturn(List.of(venta));
        when(saleMapper.toResponseList(List.of(venta))).thenReturn(List.of(response));

        List<SaleResponseDTO> result = saleService.findAll(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("shouldFindSaleByIdWhenSaleBelongsToEmpresa")
    void shouldFindSaleByIdWhenSaleBelongsToEmpresa() {
        Usuario usuario = crearUsuario();
        Venta venta = Venta.builder()
                .id(1L)
                .empresa(usuario.getEmpresa())
                .usuario(usuario)
                .build();

        SaleResponseDTO response = SaleResponseDTO.builder()
                .id(1L)
                .empresaId(1L)
                .build();

        when(ventaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(venta));
        when(saleMapper.toResponse(venta)).thenReturn(response);

        SaleResponseDTO result = saleService.findById(1L, usuario);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenSaleDoesNotBelongToEmpresa")
    void shouldThrowResourceNotFoundExceptionWhenSaleDoesNotBelongToEmpresa() {
        Usuario usuario = crearUsuario();

        when(ventaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> saleService.findById(99L, usuario));
    }

    private Usuario crearUsuario() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .nombre("Empresa Test")
                .build();

        return Usuario.builder()
                .id(1L)
                .username("luis")
                .empresa(empresa)
                .build();
    }

    private Producto crearProducto(Integer stock) {
        return Producto.builder()
                .id(1L)
                .nombre("Mouse")
                .sku("SKU-001")
                .stockActual(stock)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("100.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresa(Empresa.builder().id(1L).build())
                .build();
    }

    private SaleRequestDTO crearSaleRequest(Long productoId, Integer cantidad) {
        SaleRequestDTO request = new SaleRequestDTO();
        request.setItems(List.of(crearItem(productoId, cantidad)));
        return request;
    }

    private SaleItemRequestDTO crearItem(Long productoId, Integer cantidad) {
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        return item;
    }
}