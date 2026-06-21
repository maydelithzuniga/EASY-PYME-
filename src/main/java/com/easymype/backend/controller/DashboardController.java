package com.easymype.backend.controller;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DashboardSummaryDTO> summary(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(dashboardService.getSummary(usuario));
    }
}