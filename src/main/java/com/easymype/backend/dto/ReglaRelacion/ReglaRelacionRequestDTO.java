package com.easymype.backend.dto.ReglaRelacion;
import com.easymype.backend.entity.TipoDisparo;
import lombok.Data;

import java.util.List;

@Data
public class ReglaRelacionRequestDTO {

    private String nombre;
    private Long tablaOrigenId;
    private Long columnaTriggerId;
    private Long columnaCondicionOrigenId; // opcional
    private String valorCondicionOrigen;    // opcional
    private TipoDisparo tipoDisparo;
    private List<AccionReglaRequestDTO> acciones;
}
