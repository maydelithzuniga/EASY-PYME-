package com.easymype.backend.controller;

import com.easymype.backend.dto.alert.AlertResponseDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<AlertResponseDTO>> findAll(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(alertService.findAll(usuario));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<AlertResponseDTO> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(alertService.markAsRead(id, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        alertService.delete(id, usuario);
        return ResponseEntity.noContent().build();
    }
}