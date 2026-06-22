package com.easymype.backend.repository;

import com.easymype.backend.dto.dashboard.ProductoAlertaDTO;
import com.easymype.backend.dto.dashboard.ProductoInmovilizadoDTO;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        SELECT COALESCE(SUM(d.cantidad * d.costoUnitario), 0)
        FROM DetalleVenta d
        JOIN d.venta v
        WHERE v.empresa.id = :empresaId
          AND v.fecha BETWEEN :desde AND :hasta
    """)
    BigDecimal sumCostoVentasByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);


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
            Pageable pageable);

@Query("""
        SELECT new com.easymype.backend.dto.dashboard.ProductoInmovilizadoDTO(
            p.id,
            p.nombre,
            p.sku,
            p.stockActual,
            p.precio,
            p.costo,
            CAST(p.stockActual * p.costo AS bigdecimal),
            CAST(DATEDIFF(CURRENT_DATE, COALESCE(MAX(v.fecha), p.fechaCreacion)) AS long)
        )
        FROM Producto p
        LEFT JOIN DetalleVenta d ON d.producto.id = p.id
        LEFT JOIN d.venta v ON v.fecha BETWEEN :desde AND :hasta
        WHERE p.empresa.id = :empresaId
          AND d.id IS NULL
        GROUP BY p.id
        ORDER BY (p.stockActual * p.costo) DESC
    """)
List<ProductoInmovilizadoDTO> findTopInmovilizadosByEmpresaId(
        @Param("empresaId") Long empresaId,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable);

// Rotación: inventario promedio valorizado
@Query("""
        SELECT COALESCE(SUM(p.stockActual * p.costo), 0)
        FROM Producto p
        WHERE p.empresa.id = :empresaId
    """)
BigDecimal sumInventarioValorizadoByEmpresaId(@Param("empresaId") Long empresaId);}