package com.easymype.backend.repository;

import com.easymype.backend.entity.Celda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CeldaRepository extends JpaRepository<Celda, Long> {
}

