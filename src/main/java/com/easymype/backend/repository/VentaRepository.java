package com.easymype.backend.repository;

import com.easymype.backend.dto.dashboard.EvolucionDiariaDTO;
import com.easymype.backend.dto.dashboard.ProductoTopVentaDTO;
import com.easymype.backend.dto.dashboard.RentabilidadProductoDTO;
import com.easymype.backend.entity.Venta;
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
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByEmpresaId(Long empresaId);

    @Query("""
    SELECT new com.easymype.backend.dto.dashboard.ProductoTopVentaDTO(
        p.id, p.nombre, p.sku, SUM(dv.cantidad), SUM(dv.cantidad * dv.precioUnitario))
    FROM DetalleVenta dv
    JOIN dv.venta v
    JOIN dv.producto p
    WHERE v.empresa.id = :empresaId
    AND v.fecha BETWEEN :inicio AND :fin
    GROUP BY p.id, p.nombre, p.sku
    ORDER BY SUM(dv.cantidad) DESC
    """)
    List<ProductoTopVentaDTO> findTopProductosByEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);

    Optional<Venta> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresa.id = :empresaId AND v.fecha >= :desde AND v.fecha <= :hasta")
    long countByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                                         @Param("desde") LocalDateTime desde,
                                         @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.empresa.id = :empresaId AND v.fecha >= :desde AND v.fecha <= :hasta")
    BigDecimal sumTotalByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                                                  @Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta);
    @Query("""
        SELECT new com.easymype.backend.dto.dashboard.EvolucionDiariaDTO(
            CAST(v.fecha AS localdate),
            SUM(v.total),
            COUNT(v.id)
        )
        FROM Venta v
        WHERE v.empresa.id = :empresaId
          AND v.fecha BETWEEN :desde AND :hasta
        GROUP BY CAST(v.fecha AS localdate)
        ORDER BY CAST(v.fecha AS localdate) ASC
    """)
    List<EvolucionDiariaDTO> findEvolucionDiaria(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Rentabilidad por producto (requiere campo costoUnitario en DetalleVenta)
    @Query("""
        SELECT new com.easymype.backend.dto.dashboard.RentabilidadProductoDTO(
            p.id,
            p.nombre,
            p.sku,
            SUM(d.subtotal),
            SUM(d.cantidad * d.costoUnitario),
            SUM(d.subtotal - d.cantidad * d.costoUnitario),
            CASE WHEN SUM(d.subtotal) > 0
                 THEN (SUM(d.subtotal - d.cantidad * d.costoUnitario) / SUM(d.subtotal)) * 100
                 ELSE 0 END,
            SUM(d.cantidad)
        )
        FROM DetalleVenta d
        JOIN d.producto p
        JOIN d.venta v
        WHERE v.empresa.id = :empresaId
          AND v.fecha BETWEEN :desde AND :hasta
        GROUP BY p.id, p.nombre, p.sku
        ORDER BY SUM(d.subtotal - d.cantidad * d.costoUnitario) DESC
    """)
    List<RentabilidadProductoDTO> findRentabilidadPorProducto(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Clientes únicos que compraron en el período
    @Query("""
        SELECT COUNT(DISTINCT v.cliente.id)
        FROM Venta v
        WHERE v.empresa.id = :empresaId
          AND v.fecha BETWEEN :desde AND :hasta
          AND v.cliente IS NOT NULL
    """)
    Long countClientesActivosByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Clientes nuevos: primera compra dentro del período
    @Query("""
        SELECT COUNT(DISTINCT v.cliente.id)
        FROM Venta v
        WHERE v.empresa.id = :empresaId
          AND v.cliente IS NOT NULL
          AND v.fecha BETWEEN :desde AND :hasta
          AND NOT EXISTS (
              SELECT 1 FROM Venta v2
              WHERE v2.cliente.id = v.cliente.id
                AND v2.empresa.id = :empresaId
                AND v2.fecha < :desde
          )
    """)
    Long countClientesNuevosByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Frecuencia promedio de compra por cliente
    @Query("""
        SELECT COALESCE(AVG(sub.total_compras), 0)
        FROM (
            SELECT COUNT(v.id) AS total_compras
            FROM Venta v
            WHERE v.empresa.id = :empresaId
              AND v.cliente IS NOT NULL
              AND v.fecha BETWEEN :desde AND :hasta
            GROUP BY v.cliente.id
        ) sub
    """)
    BigDecimal avgFrecuenciaCompraByEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    BigDecimal sumCostoVentasByEmpresaIdAndFechaBetween(Long empresaId, LocalDateTime desde, LocalDateTime hasta);
}

