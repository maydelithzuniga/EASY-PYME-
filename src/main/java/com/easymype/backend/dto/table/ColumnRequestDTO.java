package com.easymype.backend.dto.table;

import com.easymype.backend.entity.TipoColumna;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ColumnRequestDTO {

    @NotBlank(message = "El nombre de la columna es obligatorio")
    private String nombre;

    private Integer orden;

    @NotNull(message = "El tipo de columna es obligatorio")
    private TipoColumna tipo;

    private Boolean esRequerida;

    private String valorDefault;

    // Solo requerido si tipo == CATEGORIA
    private List<Long> categoriaIds;
}