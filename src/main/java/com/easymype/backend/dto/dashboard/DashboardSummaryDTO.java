package com.easymype.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalProductos;
    private long productosStockBajo;
    private long productosAgotados;
    private long ventasDelMes;
    private BigDecimal ingresosDelMes;
}
