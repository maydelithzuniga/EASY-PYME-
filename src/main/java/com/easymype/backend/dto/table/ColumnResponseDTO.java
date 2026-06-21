package com.easymype.backend.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnResponseDTO {
    private Long id;
    private String nombre;
    private Integer orden;
}
