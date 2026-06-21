package com.easymype.backend.repository;

import com.easymype.backend.dto.dashboard.ProductoTopVentaDTO;
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
}

