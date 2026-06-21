package com.easymype.backend.service;

import com.easymype.backend.dto.product.ProductRequestDTO;
import com.easymype.backend.dto.product.ProductResponseDTO;
import com.easymype.backend.entity.*;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.ProductMapper;
import com.easymype.backend.repository.CategoriaRepository;
import com.easymype.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        if (productoRepository.existsBySkuAndEmpresaId(request.getSku(), empresaId)) {
            throw new ConflictException("Ya existe un producto con SKU '" + request.getSku() + "' en su empresa");
        }

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .sku(request.getSku())
                .stockActual(request.getStockActual())
                .stockMinimo(request.getStockMinimo())
                .precioVenta(request.getPrecioVenta())
                .costo(request.getCosto())
                .empresa(usuario.getEmpresa())
                .categorias(new ArrayList<>())
                .build();

        producto.recalcularEstadoStock();

        if (request.getCategoriaIds() != null && !request.getCategoriaIds().isEmpty()) {
            List<Categoria> categorias = resolverCategorias(request.getCategoriaIds(), empresaId);
            producto.getCategorias().addAll(categorias);
        }

        return productMapper.toResponse(productoRepository.save(producto));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll(Usuario usuario) {
        return productMapper.toResponseList(
                productoRepository.findByEmpresaId(usuario.getEmpresa().getId())
        );
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id, Usuario usuario) {
        return productMapper.toResponse(getProductoOrThrow(id, usuario.getEmpresa().getId()));
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        Producto producto = getProductoOrThrow(id, empresaId);

        if (productoRepository.existsBySkuAndEmpresaIdAndIdNot(request.getSku(), empresaId, id)) {
            throw new ConflictException("Ya existe otro producto con SKU '" + request.getSku() + "'");
        }

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setSku(request.getSku());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setCosto(request.getCosto());
        producto.recalcularEstadoStock();

        producto.getCategorias().clear();
        if (request.getCategoriaIds() != null && !request.getCategoriaIds().isEmpty()) {
            producto.getCategorias().addAll(resolverCategorias(request.getCategoriaIds(), empresaId));
        }

        return productMapper.toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void delete(Long id, Usuario usuario) {
        Producto producto = getProductoOrThrow(id, usuario.getEmpresa().getId());
        productoRepository.delete(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findLowStock(Usuario usuario) {
        return productMapper.toResponseList(
                productoRepository.findByEmpresaIdAndEstadoStock(
                        usuario.getEmpresa().getId(), EstadoStock.BAJO)
        );
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findOutOfStock(Usuario usuario) {
        return productMapper.toResponseList(
                productoRepository.findByEmpresaIdAndEstadoStock(
                        usuario.getEmpresa().getId(), EstadoStock.AGOTADO)
        );
    }

    public Producto getProductoOrThrow(Long id, Long empresaId) {
        return productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto con id=" + id + " no encontrado en su empresa"));
    }

    private List<Categoria> resolverCategorias(List<Long> ids, Long empresaId) {
        return ids.stream()
                .map(catId -> categoriaRepository.findByIdAndEmpresaId(catId, empresaId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Categoría con id=" + catId + " no encontrada en su empresa")))
                .toList();
    }
}
