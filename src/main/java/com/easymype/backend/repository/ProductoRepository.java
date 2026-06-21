package com.easymype.backend.repository;

import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
