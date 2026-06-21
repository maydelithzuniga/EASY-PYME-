package com.easymype.backend.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowResponseDTO {
    private Long id;
    private Integer orden;
    private Long productoId;
    private List<CellResponseDTO> celdas;
}
