package com.easymype.backend.dto.ReglaRelacion;
import com.easymype.backend.entity.TipoDisparo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReglaRelacionResponseDTO {

    private Long id;
    private String nombre;
    private Long tablaOrigenId;
    private String tablaOrigenNombre;
    private Long columnaTriggerId;
    private String columnaTriggerNombre;
    private TipoDisparo tipoDisparo;
    private boolean activa;
    private List<AccionReglaResponseDTO> acciones;
}
