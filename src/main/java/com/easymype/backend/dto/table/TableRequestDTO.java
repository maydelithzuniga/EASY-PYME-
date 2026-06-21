package com.easymype.backend.dto.table;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TableRequestDTO {

    @NotBlank(message = "El nombre de la tabla es obligatorio")
    private String nombre;
    private Long plantillaId;
    private Long proyectoId;
}
