package com.easymype.backend.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponseDTO {
    private Long id;
    private String nombre;
    private Long empresaId;
    private Long proyectoId;
    private List<ColumnResponseDTO> columnas;
    private List<RowResponseDTO> filas;
    private LocalDateTime createdAt;
}
