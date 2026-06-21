package com.easymype.backend.mapper;

import com.easymype.backend.dto.table.*;
import com.easymype.backend.entity.Celda;
import com.easymype.backend.entity.Columna;
import com.easymype.backend.entity.Fila;
import com.easymype.backend.entity.TablaInventario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableMapper {

    public TableResponseDTO toResponse(TablaInventario tabla) {
        return TableResponseDTO.builder()
                .id(tabla.getId())
                .nombre(tabla.getNombre())
                .empresaId(tabla.getEmpresa().getId())
                .proyectoId(tabla.getProyecto() != null ? tabla.getProyecto().getId() : null)
                .columnas(tabla.getColumnas().stream().map(this::toColumnResponse).toList())
                .filas(tabla.getFilas().stream().map(this::toRowResponse).toList())
                .createdAt(tabla.getCreatedAt())
                .build();
    }

    public ColumnResponseDTO toColumnResponse(Columna columna) {
        return ColumnResponseDTO.builder()
                .id(columna.getId())
                .nombre(columna.getNombre())
                .orden(columna.getOrden())
                .build();
    }

    public RowResponseDTO toRowResponse(Fila fila) {
        List<CellResponseDTO> celdas = fila.getCeldas().stream()
                .map(this::toCellResponse)
                .toList();
        return RowResponseDTO.builder()
                .id(fila.getId())
                .orden(fila.getOrden())
                .productoId(fila.getProducto() != null ? fila.getProducto().getId() : null)
                .celdas(celdas)
                .build();
    }

    public CellResponseDTO toCellResponse(Celda celda) {
        return CellResponseDTO.builder()
                .id(celda.getId())
                .columnaId(celda.getColumna().getId())
                .valorTexto(celda.getValorTexto())
                .build();
    }

    public List<TableResponseDTO> toResponseList(List<TablaInventario> tablas) {
        return tablas.stream().map(this::toResponse).toList();
    }
}
