package com.easymype.backend.controller;

import com.easymype.backend.dto.sale.SaleRequestDTO;
import com.easymype.backend.dto.sale.SaleResponseDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SaleResponseDTO> create(
            @Valid @RequestBody SaleRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.registrarVenta(request, usuario));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<SaleResponseDTO>> findAll(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(saleService.findAll(usuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SaleResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(saleService.findById(id, usuario));
    }
}
