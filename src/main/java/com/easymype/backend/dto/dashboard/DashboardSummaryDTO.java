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
    // ── Lo que ya tenía Summary ──
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

    // ── Lo que tenía Advanced ──
    private BigDecimal ventasHoy;
    private BigDecimal ventasSemana;
    private BigDecimal ventasMes;
    private BigDecimal ventasAnio;
    private BigDecimal crecimientoPorcentual;
    private BigDecimal ticketPromedio;
    private BigDecimal margenBrutoPorcentual;
    private BigDecimal margenNetoPorcentual;
    private BigDecimal utilidadNeta;
    private BigDecimal utilidadAcumuladaAnio;
    private BigDecimal costoProductosVendidos;
    private BigDecimal costosOperativos;
    private BigDecimal flujoCajaEntradas;
    private BigDecimal flujoCajaSalidas;
    private BigDecimal flujoCajaNeto;
    private BigDecimal flujoCajaProyectado30dias;
    private BigDecimal puntoEquilibrio;
    private BigDecimal margenContribucionPorcentual;
    private BigDecimal valorInventarioInmovilizado;
    private BigDecimal rotacionInventario;
    private List<ProductoInmovilizadoDTO> topProductosInmovilizados;
    private List<EvolucionDiariaDTO> evolucionDiaria;
    private List<RentabilidadProductoDTO> rentabilidadPorProducto;
    private BigDecimal cac;
    private BigDecimal ltv;
    private BigDecimal relacionLtvCac;
    private Long clientesNuevos;
    private Long totalClientes;
    private BigDecimal tasaConversion;
    private BigDecimal roiPublicidad;
    private BigDecimal gastoMarketing;
}
