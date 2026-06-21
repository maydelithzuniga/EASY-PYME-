package com.easymype.backend.service;

import com.easymype.backend.dto.product.ProductRequestDTO;
import com.easymype.backend.dto.product.ProductResponseDTO;
import com.easymype.backend.entity.Categoria;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.ProductMapper;
import com.easymype.backend.repository.CategoriaRepository;
import com.easymype.backend.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("shouldCreateProductWhenSkuIsAvailable")
    void shouldCreateProductWhenSkuIsAvailable() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();
        Producto producto = crearProducto();
        ProductResponseDTO response = crearProductResponse();

        when(productoRepository.existsBySkuAndEmpresaId("SKU-001", 1L)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productMapper.toResponse(producto)).thenReturn(response);

        ProductResponseDTO result = productService.create(request, usuario);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Laptop");
        verify(productoRepository).existsBySkuAndEmpresaId("SKU-001", 1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenSkuAlreadyExists")
    void shouldThrowConflictExceptionWhenSkuAlreadyExists() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();

        when(productoRepository.existsBySkuAndEmpresaId("SKU-001", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> productService.create(request, usuario));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("shouldCreateProductWithCategoriesWhenCategoryIdsAreProvided")
    void shouldCreateProductWithCategoriesWhenCategoryIdsAreProvided() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();
        request.setCategoriaIds(List.of(10L));

        Categoria categoria = Categoria.builder()
                .id(10L)
                .nombre("Tecnología")
                .empresa(usuario.getEmpresa())
                .build();

        Producto producto = crearProducto();
        ProductResponseDTO response = crearProductResponse();

        when(productoRepository.existsBySkuAndEmpresaId("SKU-001", 1L)).thenReturn(false);
        when(categoriaRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productMapper.toResponse(producto)).thenReturn(response);

        ProductResponseDTO result = productService.create(request, usuario);

        assertThat(result).isNotNull();
        verify(categoriaRepository).findByIdAndEmpresaId(10L, 1L);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCategoryDoesNotBelongToEmpresa")
    void shouldThrowResourceNotFoundExceptionWhenCategoryDoesNotBelongToEmpresa() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();
        request.setCategoriaIds(List.of(10L));

        when(productoRepository.existsBySkuAndEmpresaId("SKU-001", 1L)).thenReturn(false);
        when(categoriaRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(request, usuario));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("shouldFindAllProductsByEmpresa")
    void shouldFindAllProductsByEmpresa() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto();
        ProductResponseDTO response = crearProductResponse();

        when(productoRepository.findByEmpresaId(1L)).thenReturn(List.of(producto));
        when(productMapper.toResponseList(List.of(producto))).thenReturn(List.of(response));

        List<ProductResponseDTO> result = productService.findAll(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Laptop");
        verify(productoRepository).findByEmpresaId(1L);
    }

    @Test
    @DisplayName("shouldFindProductByIdWhenProductBelongsToEmpresa")
    void shouldFindProductByIdWhenProductBelongsToEmpresa() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto();
        ProductResponseDTO response = crearProductResponse();

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(productMapper.toResponse(producto)).thenReturn(response);

        ProductResponseDTO result = productService.findById(1L, usuario);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
        Usuario usuario = crearUsuario();

        when(productoRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L, usuario));
    }

    @Test
    @DisplayName("shouldUpdateProductWhenDataIsValid")
    void shouldUpdateProductWhenDataIsValid() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();
        request.setNombre("Laptop actualizada");

        Producto producto = crearProducto();
        ProductResponseDTO response = crearProductResponse();
        response.setNombre("Laptop actualizada");

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsBySkuAndEmpresaIdAndIdNot("SKU-001", 1L, 1L)).thenReturn(false);
        when(productoRepository.save(producto)).thenReturn(producto);
        when(productMapper.toResponse(producto)).thenReturn(response);

        ProductResponseDTO result = productService.update(1L, request, usuario);

        assertThat(result.getNombre()).isEqualTo("Laptop actualizada");
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenUpdatingSkuToExistingSku")
    void shouldThrowConflictExceptionWhenUpdatingSkuToExistingSku() {
        Usuario usuario = crearUsuario();
        ProductRequestDTO request = crearProductRequest();
        Producto producto = crearProducto();

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsBySkuAndEmpresaIdAndIdNot("SKU-001", 1L, 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> productService.update(1L, request, usuario));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("shouldDeleteProductWhenProductExists")
    void shouldDeleteProductWhenProductExists() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto();

        when(productoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(producto));

        productService.delete(1L, usuario);

        verify(productoRepository).delete(producto);
    }

    @Test
    @DisplayName("shouldFindLowStockProducts")
    void shouldFindLowStockProducts() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto();
        producto.setEstadoStock(EstadoStock.BAJO);

        ProductResponseDTO response = crearProductResponse();
        response.setEstadoStock(EstadoStock.BAJO);

        when(productoRepository.findByEmpresaIdAndEstadoStock(1L, EstadoStock.BAJO))
                .thenReturn(List.of(producto));
        when(productMapper.toResponseList(List.of(producto))).thenReturn(List.of(response));

        List<ProductResponseDTO> result = productService.findLowStock(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstadoStock()).isEqualTo(EstadoStock.BAJO);
    }

    @Test
    @DisplayName("shouldFindOutOfStockProducts")
    void shouldFindOutOfStockProducts() {
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto();
        producto.setEstadoStock(EstadoStock.AGOTADO);

        ProductResponseDTO response = crearProductResponse();
        response.setEstadoStock(EstadoStock.AGOTADO);

        when(productoRepository.findByEmpresaIdAndEstadoStock(1L, EstadoStock.AGOTADO))
                .thenReturn(List.of(producto));
        when(productMapper.toResponseList(List.of(producto))).thenReturn(List.of(response));

        List<ProductResponseDTO> result = productService.findOutOfStock(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstadoStock()).isEqualTo(EstadoStock.AGOTADO);
    }

    private Usuario crearUsuario() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .nombre("Empresa Test")
                .build();

        return Usuario.builder()
                .id(1L)
                .username("luis")
                .email("luis@test.com")
                .empresa(empresa)
                .build();
    }

    private ProductRequestDTO crearProductRequest() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Laptop");
        request.setDescripcion("Laptop Lenovo");
        request.setSku("SKU-001");
        request.setStockActual(10);
        request.setStockMinimo(2);
        request.setPrecioVenta(new BigDecimal("2500.00"));
        request.setCosto(new BigDecimal("2000.00"));
        return request;
    }

    private Producto crearProducto() {
        return Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Laptop Lenovo")
                .sku("SKU-001")
                .stockActual(10)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("2500.00"))
                .costo(new BigDecimal("2000.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresa(Empresa.builder().id(1L).nombre("Empresa Test").build())
                .build();
    }

    private ProductResponseDTO crearProductResponse() {
        return ProductResponseDTO.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Laptop Lenovo")
                .sku("SKU-001")
                .stockActual(10)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("2500.00"))
                .costo(new BigDecimal("2000.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresaId(1L)
                .categorias(List.of())
                .build();
    }
}