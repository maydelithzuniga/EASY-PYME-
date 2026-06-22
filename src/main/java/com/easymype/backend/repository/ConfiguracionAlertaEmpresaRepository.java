package com.easymype.backend.repository;

import com.easymype.backend.event.ConfiguracionAlertaEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfiguracionAlertaEmpresaRepository extends JpaRepository<ConfiguracionAlertaEmpresa, Long> {
    List<ConfiguracionAlertaEmpresa> findByEmpresaIdAndRecibeAlertasTrue(Long empresaId);

}
