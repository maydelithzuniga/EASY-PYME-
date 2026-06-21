package com.easymype.backend.repository;

import com.easymype.backend.entity.PlantillaTabla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantillaTablaRepository extends JpaRepository<PlantillaTabla, Long> {
}
