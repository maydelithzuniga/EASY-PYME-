package com.easymype.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;
}
