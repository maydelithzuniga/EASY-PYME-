package com.easymype.backend.dto.alert;

import com.easymype.backend.entity.TipoAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private TipoAlerta tipo;
    private String mensaje;
    private boolean leida;
    private LocalDateTime createdAt;
}
