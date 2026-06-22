package com.easymype.backend.controller;
import com.easymype.backend.dto.ReglaRelacion.ReglaRelacionRequestDTO;
import com.easymype.backend.dto.ReglaRelacion.ReglaRelacionResponseDTO;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.service.ReglaRelacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reglas-relacion")
@RequiredArgsConstructor
public class ReglaRelacionController {

    private final ReglaRelacionService reglaRelacionService;

    @PostMapping
    public ResponseEntity<ReglaRelacionResponseDTO> create(
            @RequestBody ReglaRelacionRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        ReglaRelacionResponseDTO response = reglaRelacionService.create(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReglaRelacionResponseDTO>> findAll(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reglaRelacionService.findAllByEmpresa(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReglaRelacionResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reglaRelacionService.findById(id, usuario));
    }

    @GetMapping("/tabla/{tablaId}")
    public ResponseEntity<List<ReglaRelacionResponseDTO>> findByTablaOrigen(
            @PathVariable Long tablaId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reglaRelacionService.findByTablaOrigen(tablaId, usuario));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ReglaRelacionResponseDTO> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reglaRelacionService.cambiarEstado(id, true, usuario));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ReglaRelacionResponseDTO> desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reglaRelacionService.cambiarEstado(id, false, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        reglaRelacionService.delete(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
