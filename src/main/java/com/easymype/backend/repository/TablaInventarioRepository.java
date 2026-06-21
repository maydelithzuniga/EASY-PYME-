package com.easymype.backend.repository;

import com.easymype.backend.entity.TablaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablaInventarioRepository extends JpaRepository<TablaInventario, Long> {

    List<TablaInventario> findByEmpresaId(Long empresaId);

    Optional<TablaInventario> findByIdAndEmpresaId(Long id, Long empresaId);
}
