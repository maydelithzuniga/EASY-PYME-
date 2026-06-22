package com.easymype.backend.dto.ReglaRelacion;
import com.easymype.backend.entity.ModoCalculo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AccionReglaResponseDTO {
    private Long id;
    private Long tablaDestinoId;
    private String tablaDestinoNombre;
    private Long columnaDestinoId;
    private String columnaDestinoNombre;
    private Long columnaCondicionDestinoId;
    private String columnaCondicionDestinoNombre;
    private List<String> valoresCondicionDestino;
    private ModoCalculo modoCalculo;
    private BigDecimal factor;
}
