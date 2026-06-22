package com.easymype.backend.controller;

import com.easymype.backend.dto.plantilla.PlantillaColumnaResponseDTO;
import com.easymype.backend.dto.plantilla.PlantillaResponseDTO;
import com.easymype.backend.repository.PlantillaTablaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plantillas")
@RequiredArgsConstructor

public class PlantillaController {
    private final PlantillaTablaRepository plantillaRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<PlantillaResponseDTO>> findAll() {
        return ResponseEntity.ok(
                plantillaRepository.findAll().stream()
                        .map(p -> PlantillaResponseDTO.builder()
                                .id(p.getId())
                                .nombre(p.getNombre())
                                .descripcion(p.getDescripcion())
                                .icono(p.getIcono())
                                .categoria(p.getCategoria())
                                .columnas(p.getColumnas().stream()
                                        .map(c -> PlantillaColumnaResponseDTO.builder()
                                                .nombre(c.getNombre())
                                                .tipo(c.getTipo())
                                                .esRequerida(c.getEsRequerida())
                                                .orden(c.getOrden())
                                                .build())
                                        .toList())
                                .build())
                        .toList()
        );
    }
}
