package com.easymype.backend.dto.ReglaRelacion;
import com.easymype.backend.entity.ModoCalculo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccionReglaRequestDTO {
    private Long tablaDestinoId;
    private Long columnaDestinoId;
    private Long columnaCondicionDestinoId;       // opcional, null = todas las filas
    private List<String> valoresCondicionDestino; // opcional
    private ModoCalculo modoCalculo;
    private BigDecimal factor;
}
