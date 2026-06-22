package com.easymype.backend.controller;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DashboardSummaryDTO> summary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal Usuario usuario) {

        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fin = hasta != null ? hasta.atTime(23, 59, 59) : null;

        return ResponseEntity.ok(dashboardService.getSummary(usuario, inicio, fin));
    }
}