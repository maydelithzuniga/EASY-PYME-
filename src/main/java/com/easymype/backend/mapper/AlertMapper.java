package com.easymype.backend.mapper;

import com.easymype.backend.dto.alert.AlertResponseDTO;
import com.easymype.backend.entity.AlertaInventario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertMapper {

    public AlertResponseDTO toResponse(AlertaInventario alerta) {
        return AlertResponseDTO.builder()
                .id(alerta.getId())
                .productoId(alerta.getProducto().getId())
                .productoNombre(alerta.getProducto().getNombre())
                .tipo(alerta.getTipo())
                .mensaje(alerta.getMensaje())
                .leida(alerta.isLeida())
                .createdAt(alerta.getCreatedAt())
                .build();
    }

    public List<AlertResponseDTO> toResponseList(List<AlertaInventario> alertas) {
        return alertas.stream().map(this::toResponse).toList();
    }
}
