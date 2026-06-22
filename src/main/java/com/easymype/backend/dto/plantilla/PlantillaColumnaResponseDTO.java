package com.easymype.backend.dto.plantilla;

import com.easymype.backend.entity.TipoColumna;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlantillaColumnaResponseDTO {
    private String nombre;
    private TipoColumna tipo;
    private boolean esRequerida;
    private int orden;
}
