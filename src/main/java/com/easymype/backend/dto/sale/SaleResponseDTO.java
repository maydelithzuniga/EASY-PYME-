package com.easymype.backend.dto.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDTO {
    private Long id;
    private Long empresaId;
    private String usuarioUsername;
    private LocalDateTime fecha;
    private BigDecimal total;
    private List<SaleItemResponseDTO> detalles;
    private LocalDateTime createdAt;
}
