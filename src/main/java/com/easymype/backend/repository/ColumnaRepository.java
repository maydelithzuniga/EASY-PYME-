package com.easymype.backend.repository;

import com.easymype.backend.entity.Columna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnaRepository extends JpaRepository<Columna, Long> {
}

