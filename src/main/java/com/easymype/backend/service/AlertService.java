package com.easymype.backend.service;

import com.easymype.backend.dto.alert.AlertResponseDTO;
import com.easymype.backend.entity.*;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.AlertMapper;
import com.easymype.backend.repository.AlertaInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertaInventarioRepository alertaRepository;
    private final AlertMapper alertMapper;

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findAll(Usuario usuario) {
        return alertMapper.toResponseList(
                alertaRepository.findByEmpresaIdOrderByCreatedAtDesc(usuario.getEmpresa().getId())
        );
    }

    @Transactional
    public AlertResponseDTO markAsRead(Long id, Usuario usuario) {
        AlertaInventario alerta = getAlertaOrThrow(id, usuario.getEmpresa().getId());
        alerta.setLeida(true);
        return alertMapper.toResponse(alertaRepository.save(alerta));
    }

    @Transactional
    public void delete(Long id, Usuario usuario) {
        AlertaInventario alerta = getAlertaOrThrow(id, usuario.getEmpresa().getId());
        alertaRepository.delete(alerta);
    }

    private AlertaInventario getAlertaOrThrow(Long id, Long empresaId) {
        return alertaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta con id=" + id + " no encontrada"));
    }

}
