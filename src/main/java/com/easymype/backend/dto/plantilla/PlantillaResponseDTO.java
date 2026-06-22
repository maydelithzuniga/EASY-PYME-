package com.easymype.backend.dto.plantilla;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlantillaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String categoria;
    private List<PlantillaColumnaResponseDTO> columnas;
}
