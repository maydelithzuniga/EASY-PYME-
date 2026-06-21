package com.easymype.backend.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CellResponseDTO {
    private Long id;
    private Long columnaId;
    private String valorTexto;
}
