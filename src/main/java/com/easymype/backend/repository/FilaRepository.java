package com.easymype.backend.repository;

import com.easymype.backend.entity.Fila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilaRepository extends JpaRepository<Fila, Long> {
    List<Fila> findByTablaId(Long id);
}
