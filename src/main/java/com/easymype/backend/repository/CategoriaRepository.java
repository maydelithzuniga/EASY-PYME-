package com.easymype.backend.repository;

import com.easymype.backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEmpresaId(Long empresaId);

    Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);
}