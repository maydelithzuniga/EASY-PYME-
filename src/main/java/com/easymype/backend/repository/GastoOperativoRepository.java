package com.easymype.backend.repository;

import com.easymype.backend.entity.CategoriaGasto;
import com.easymype.backend.entity.GastoOperativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GastoOperativoRepository extends JpaRepository<GastoOperativo, Long> {

    @Query("""
        SELECT COALESCE(SUM(g.monto), 0)
        FROM GastoOperativo g
        WHERE g.empresa.id = :empresaId
          AND g.fecha BETWEEN :desde AND :hasta
    """)
    BigDecimal sumByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    // Solo gastos fijos (para punto de equilibrio)
    @Query("""
        SELECT COALESCE(SUM(g.monto), 0)
        FROM GastoOperativo g
        WHERE g.empresa.id = :empresaId
          AND g.esFijo = true
          AND g.fecha BETWEEN :desde AND :hasta
    """)
    BigDecimal sumFijosByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    // Solo gastos de marketing (para CAC y ROI)
    @Query("""
        SELECT COALESCE(SUM(g.monto), 0)
        FROM GastoOperativo g
        WHERE g.empresa.id = :empresaId
          AND g.categoria = :categoria
          AND g.fecha BETWEEN :desde AND :hasta
    """)
    BigDecimal sumByCategoriaAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("categoria") CategoriaGasto categoria,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

}
