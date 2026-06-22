package com.easymype.backend.repository;

import com.easymype.backend.entity.AlertaInventario;
import com.easymype.backend.entity.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaInventarioRepository extends JpaRepository<AlertaInventario, Long> {

    List<AlertaInventario> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

    Optional<AlertaInventario> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByProductoIdAndTipoAndLeidaFalse(Long productoId, TipoAlerta tipo);
}