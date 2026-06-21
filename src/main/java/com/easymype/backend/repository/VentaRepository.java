package com.easymype.backend.repository;

import com.easymype.backend.entity.Venta;
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

