package com.easymype.backend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardAdvancedDTO {

    // ── Ventas temporales ─────────────────────────────────────────────────────
    private BigDecimal ventasHoy;
    private BigDecimal ventasSemana;
    private BigDecimal ventasMes;
    private BigDecimal ventasAnio;
    private BigDecimal crecimientoPorcentual;         // vs período anterior

    // ── Ticket promedio ───────────────────────────────────────────────────────
    private BigDecimal ticketPromedio;                // Ventas / Nº pedidos

    // ── Rentabilidad ─────────────────────────────────────────────────────────
    private BigDecimal margenBrutoPorcentual;         // (Ventas - CostoProductos) / Ventas * 100
    private BigDecimal margenNetoPorcentual;          // UtilidadNeta / Ventas * 100
    private BigDecimal utilidadNeta;                  // Ventas - CostoProductos - CostosOperativos
    private BigDecimal utilidadAcumuladaAnio;

    // ── Costos ────────────────────────────────────────────────────────────────
    private BigDecimal costoProductosVendidos;        // SUM(costo * cantidad) en período
    private BigDecimal costosOperativos;              // suma de gastos registrados en período

    // ── Flujo de caja ─────────────────────────────────────────────────────────
    private BigDecimal flujoCajaEntradas;             // ventas cobradas
    private BigDecimal flujoCajaSalidas;              // gastos pagados
    private BigDecimal flujoCajaNeto;                 // entradas - salidas
    private BigDecimal flujoCajaProyectado30dias;     // promedio diario * 30

    // ── Punto de equilibrio ───────────────────────────────────────────────────
    private BigDecimal puntoEquilibrio;               // CostosFijos / MargenContribucion
    private BigDecimal margenContribucionPorcentual;  // 1 - (CostoVariable / Ventas)

    // ── Inventario ───────────────────────────────────────────────────────────
    private BigDecimal valorInventarioInmovilizado;   // productos sin ventas en el período
    private BigDecimal rotacionInventario;            // CostoVentas / InventarioPromedio
    private List<ProductoInmovilizadoDTO> topProductosInmovilizados;

    // ── Evolución de ingresos (gráfica de línea) ─────────────────────────────
    private List<EvolucionDiariaDTO> evolucionDiaria;

    // ── Rentabilidad por producto ─────────────────────────────────────────────
    private List<RentabilidadProductoDTO> rentabilidadPorProducto;

    // ── Métricas e-commerce ───────────────────────────────────────────────────
    private BigDecimal cac;                           // GastoMarketing / ClientesNuevos
    private BigDecimal ltv;                           // TicketPromedio * Frecuencia * MesesPromedio
    private BigDecimal relacionLtvCac;                // LTV / CAC
    private Long clientesNuevos;
    private Long totalClientes;
    private BigDecimal tasaConversion;                // ClientesQueCompraron / TotalLeads * 100
    private BigDecimal roiPublicidad;                 // (Ganancia - Inversion) / Inversion * 100
    private BigDecimal gastoMarketing;
}
