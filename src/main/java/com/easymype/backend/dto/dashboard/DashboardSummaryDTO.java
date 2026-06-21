package com.easymype.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalProductos;
    private long productosStockBajo;
    private long productosAgotados;
    private long ventasDelPeriodoActual;
    private long ventasDelPeriodoAnterior;
    private BigDecimal ingresosDelPeriodo;
    private BigDecimal variacionIngresosPorcentual;
    private BigDecimal valorTotalInventario;
    private List<ProductoTopVentaDTO> topProductosVendidos;
    private List<ProductoAlertaDTO> productosAgotadosDetalle;
}
