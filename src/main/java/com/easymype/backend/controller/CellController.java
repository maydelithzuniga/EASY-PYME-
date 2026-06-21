package com.easymype.backend.controller;

import com.easymype.backend.dto.table.CellResponseDTO;
import com.easymype.backend.dto.table.CellUpdateDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cells")
@RequiredArgsConstructor
public class CellController {

    private final TableService tableService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CellResponseDTO> updateCell(
            @PathVariable Long id,
            @RequestBody CellUpdateDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tableService.updateCell(id, request, usuario));
    }
}
