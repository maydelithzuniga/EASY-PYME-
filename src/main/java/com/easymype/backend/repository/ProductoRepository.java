package com.easymype.backend.repository;

import com.easymype.backend.dto.dashboard.ProductoAlertaDTO;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEmpresaId(Long empresaId);

    Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsBySkuAndEmpresaId(String sku, Long empresaId);

    boolean existsBySkuAndEmpresaIdAndIdNot(String sku, Long empresaId, Long id);

    List<Producto> findByEmpresaIdAndEstadoStock(Long empresaId, EstadoStock estadoStock);

    long countByEmpresaId(Long empresaId);

    long countByEmpresaIdAndEstadoStock(Long empresaId, EstadoStock estadoStock);

    BigDecimal sumValorInventarioByEmpresaId(Long empresaId);

    @Query("""
    SELECT new com.easymype.backend.dto.dashboard.ProductoAlertaDTO(
        p.id, p.nombre, p.sku, p.stock, p.stockMinimo, p.estadoStock)
    FROM Producto p
    WHERE p.empresa.id = :empresaId
    AND p.estadoStock = :estadoStock
    ORDER BY p.nombre ASC
    """)
    List<ProductoAlertaDTO> findTopByEmpresaIdAndEstadoStock(
            @Param("empresaId") Long empresaId,
            @Param("estadoStock") EstadoStock estadoStock,
            Pageable pageable);}
