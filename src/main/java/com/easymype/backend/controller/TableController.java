package com.easymype.backend.controller;

import com.easymype.backend.dto.table.*;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PatchMapping("/cells/{cellId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CellResponseDTO> updateCell(
            @PathVariable Long cellId,
            @Valid @RequestBody CellUpdateDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tableService.updateCell(cellId, request, usuario));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableResponseDTO> create(
            @Valid @RequestBody TableRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.create(request, usuario));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TableResponseDTO>> findAll(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tableService.findAll(usuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TableResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tableService.findById(id, usuario));
    }

    @PostMapping("/{id}/columns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColumnResponseDTO> addColumn(
            @PathVariable Long id,
            @Valid @RequestBody ColumnRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.addColumn(id, request, usuario));
    }

    @PostMapping("/{id}/rows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RowResponseDTO> addRow(
            @PathVariable Long id, @Valid @RequestBody RowRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.addRow(id, request, usuario));
    }
}
