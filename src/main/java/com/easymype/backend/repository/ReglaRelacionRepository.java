package com.easymype.backend.repository;

import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.ReglaRelacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ReglaRelacionRepository extends JpaRepository<ReglaRelacion, Long> {
    List<ReglaRelacion> findByTablaOrigenIdAndColumnaTriggerIdAndActivaTrue(Long tablaOrigenId, Long columnaId);

    List<ReglaRelacion> findByTablaOrigen_Empresa_Id(Long empresaId);

    Optional<ReglaRelacion> findByIdAndTablaOrigen_Empresa_Id(Long id, Long empresaId);

    List<ReglaRelacion> findByTableIdAndTablaOrigen_Empresa_Id(Long tablaId, Long empresaId);
}
