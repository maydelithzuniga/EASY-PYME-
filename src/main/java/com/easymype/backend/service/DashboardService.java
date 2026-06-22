package com.easymype.backend.service;

import com.easymype.backend.dto.dashboard.*;
import com.easymype.backend.entity.*;
import com.easymype.backend.repository.GastoOperativoRepository;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final GastoOperativoRepository gastoRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(Usuario usuario, LocalDateTime desde, LocalDateTime hasta) {
        Long empresaId = usuario.getEmpresa().getId();

        if (desde == null || hasta == null) {
            YearMonth mesActual = YearMonth.now();
            desde = mesActual.atDay(1).atStartOfDay();
            hasta = mesActual.atEndOfMonth().atTime(23, 59, 59);
        }

        LocalDate desdeDate = desde.toLocalDate();
        LocalDate hastaDate = hasta.toLocalDate();

        // ── Rangos temporales fijos ──
        LocalDateTime inicioHoy    = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy       = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime inicioSemana = LocalDate.now()
                .with(java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1).atStartOfDay();
        LocalDateTime inicioMes    = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime finMes       = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime inicioAnio   = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime finAnio      = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(23, 59, 59);

        // ── Tipo de período y período anterior (una sola vez) ──
        TipoPeriodo tipo = PeriodoUtils.detectarTipoPeriodo(desde, hasta);
        LocalDateTime[] anterior = PeriodoUtils.calcularPeriodoAnterior(desde, hasta, tipo);

        // ════════════════════════════════════════════════
        // BLOQUE "SUMMARY" (lo básico)
        // ════════════════════════════════════════════════
        BigDecimal ingresos = coalesce(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, desde, hasta));
        BigDecimal ingresosPeriodoAnterior = coalesce(
                ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, anterior[0], anterior[1]));
        long ventasPeriodoAnteriorCount = ventaRepository.countByEmpresaIdAndFechaBetween(empresaId, anterior[0], anterior[1]);

        BigDecimal variacionIngresos = PeriodoUtils.calcularVariacionPorcentual(ingresos, ingresosPeriodoAnterior);

        BigDecimal valorInventario = coalesce(productoRepository.sumValorInventarioByEmpresaId(empresaId));
        List<ProductoTopVentaDTO> topProductosVendidos = ventaRepository
                .findTopProductosByEmpresaId(empresaId, desde, hasta, PageRequest.of(0, 5));
        List<ProductoAlertaDTO> productosAgotadosDetalle = productoRepository
                .findTopByEmpresaIdAndEstadoStock(empresaId, EstadoStock.AGOTADO, PageRequest.of(0, 5));

        long totalProductos = productoRepository.countByEmpresaId(empresaId);
        long stockBajo = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.BAJO);
        long agotados = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.AGOTADO);
        long ventasDelPeriodoActual = ventaRepository.countByEmpresaIdAndFechaBetween(empresaId, desde, hasta);

        // ════════════════════════════════════════════════
        // BLOQUE "ADVANCED" (lo financiero)
        // ════════════════════════════════════════════════
        BigDecimal ventasHoy    = coalesce(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicioHoy, finHoy));
        BigDecimal ventasSemana = coalesce(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicioSemana, finHoy));
        BigDecimal ventasMes    = coalesce(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicioMes, finMes));
        BigDecimal ventasAnio   = coalesce(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicioAnio, finAnio));
        BigDecimal ventasPeriodo = ingresos; // mismo valor que "ingresos", evitamos repetir query

        BigDecimal crecimiento = variacionIngresos; // mismo cálculo, reutilizado

        long cantidadVentas = ventasDelPeriodoActual;
        BigDecimal ticketPromedio = cantidadVentas > 0
                ? ventasPeriodo.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal costoProductosVendidos = coalesce(
                ventaRepository.sumCostoVentasByEmpresaIdAndFechaBetween(empresaId, desde, hasta));
        BigDecimal costosOperativos = coalesce(
                gastoRepository.sumByEmpresaIdAndFechaBetween(empresaId, desdeDate, hastaDate));
        BigDecimal costosFijos = coalesce(
                gastoRepository.sumFijosByEmpresaIdAndFechaBetween(empresaId, desdeDate, hastaDate));
        BigDecimal gastoMarketing = coalesce(
                gastoRepository.sumByCategoriaAndFechaBetween(empresaId, CategoriaGasto.MARKETING, desdeDate, hastaDate));

        BigDecimal margenBruto = calcularMargenBruto(ventasPeriodo, costoProductosVendidos);
        BigDecimal utilidadNeta = ventasPeriodo.subtract(costoProductosVendidos).subtract(costosOperativos);
        BigDecimal margenNeto = ventasPeriodo.compareTo(BigDecimal.ZERO) > 0
                ? utilidadNeta.divide(ventasPeriodo, 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal ventasAnioTotal = ventasAnio; // mismo rango, reutilizado
        BigDecimal costoAnio = coalesce(
                ventaRepository.sumCostoVentasByEmpresaIdAndFechaBetween(empresaId, inicioAnio, finAnio));
        BigDecimal gastosAnio = coalesce(
                gastoRepository.sumByEmpresaIdAndFechaBetween(empresaId, inicioAnio.toLocalDate(), finAnio.toLocalDate()));
        BigDecimal utilidadAcumuladaAnio = ventasAnioTotal.subtract(costoAnio).subtract(gastosAnio);

        BigDecimal flujoCajaEntradas = ventasPeriodo;
        BigDecimal flujoCajaSalidas  = costoProductosVendidos.add(costosOperativos);
        BigDecimal flujoCajaNeto     = flujoCajaEntradas.subtract(flujoCajaSalidas);

        long diasPeriodo = ChronoUnit.DAYS.between(desdeDate, hastaDate) + 1;
        BigDecimal promedioDiario = diasPeriodo > 0
                ? flujoCajaNeto.divide(BigDecimal.valueOf(diasPeriodo), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal flujoCajaProyectado = promedioDiario.multiply(BigDecimal.valueOf(30));

        BigDecimal costosVariables = costoProductosVendidos;
        BigDecimal margenContribucion = ventasPeriodo.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.ONE.subtract(costosVariables.divide(ventasPeriodo, 4, RoundingMode.HALF_UP))
                : BigDecimal.ZERO;
        BigDecimal puntoEquilibrio = margenContribucion.compareTo(BigDecimal.ZERO) > 0
                ? costosFijos.divide(margenContribucion, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<ProductoInmovilizadoDTO> topInmovilizados = productoRepository
                .findTopInmovilizadosByEmpresaId(empresaId, desde, hasta, PageRequest.of(0, 5));
        BigDecimal valorInmovilizado = topInmovilizados.stream()
                .map(ProductoInmovilizadoDTO::getValorInmovilizado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal inventarioActual = coalesce(productoRepository.sumInventarioValorizadoByEmpresaId(empresaId));
        BigDecimal rotacion = inventarioActual.compareTo(BigDecimal.ZERO) > 0
                ? costoProductosVendidos.divide(inventarioActual, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<EvolucionDiariaDTO> evolucionDiaria = ventaRepository.findEvolucionDiaria(empresaId, desde, hasta);
        List<RentabilidadProductoDTO> rentabilidadPorProducto = ventaRepository
                .findRentabilidadPorProducto(empresaId, desde, hasta);

        Long clientesNuevos = coalesceL(ventaRepository.countClientesNuevosByEmpresaIdAndFechaBetween(empresaId, desde, hasta));
        Long totalClientes  = coalesceL(ventaRepository.countClientesActivosByEmpresaIdAndFechaBetween(empresaId, desde, hasta));
        BigDecimal frecuencia = coalesce(ventaRepository.avgFrecuenciaCompraByEmpresaId(empresaId, desde, hasta));
        double mesesPeriodo = diasPeriodo / 30.0;

        BigDecimal cac = clientesNuevos > 0
                ? gastoMarketing.divide(BigDecimal.valueOf(clientesNuevos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal ltv = ticketPromedio
                .multiply(frecuencia)
                .multiply(BigDecimal.valueOf(mesesPeriodo))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal relacionLtvCac = cac.compareTo(BigDecimal.ZERO) > 0
                ? ltv.divide(cac, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal roiPublicidad = gastoMarketing.compareTo(BigDecimal.ZERO) > 0
                ? utilidadNeta.subtract(gastoMarketing)
                  .divide(gastoMarketing, 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal tasaConversion = BigDecimal.ZERO; // pendiente: módulo de leads

        // ════════════════════════════════════════════════
        // DTO FINAL ÚNICO
        // ════════════════════════════════════════════════
        return DashboardSummaryDTO.builder()
                // summary
                .totalProductos(totalProductos)
                .productosStockBajo(stockBajo)
                .productosAgotados(agotados)
                .ventasDelPeriodoActual(ventasDelPeriodoActual)
                .ventasDelPeriodoAnterior(ventasPeriodoAnteriorCount)
                .ingresosDelPeriodo(ingresos)
                .variacionIngresosPorcentual(variacionIngresos)
                .valorTotalInventario(valorInventario)
                .topProductosVendidos(topProductosVendidos)
                .productosAgotadosDetalle(productosAgotadosDetalle)
                // advanced
                .ventasHoy(ventasHoy)
                .ventasSemana(ventasSemana)
                .ventasMes(ventasMes)
                .ventasAnio(ventasAnio)
                .crecimientoPorcentual(crecimiento)
                .ticketPromedio(ticketPromedio)
                .margenBrutoPorcentual(margenBruto)
                .margenNetoPorcentual(margenNeto)
                .utilidadNeta(utilidadNeta)
                .utilidadAcumuladaAnio(utilidadAcumuladaAnio)
                .costoProductosVendidos(costoProductosVendidos)
                .costosOperativos(costosOperativos)
                .flujoCajaEntradas(flujoCajaEntradas)
                .flujoCajaSalidas(flujoCajaSalidas)
                .flujoCajaNeto(flujoCajaNeto)
                .flujoCajaProyectado30dias(flujoCajaProyectado)
                .puntoEquilibrio(puntoEquilibrio)
                .margenContribucionPorcentual(
                        margenContribucion.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .valorInventarioInmovilizado(valorInmovilizado)
                .rotacionInventario(rotacion)
                .topProductosInmovilizados(topInmovilizados)
                .evolucionDiaria(evolucionDiaria)
                .rentabilidadPorProducto(rentabilidadPorProducto)
                .cac(cac)
                .ltv(ltv)
                .relacionLtvCac(relacionLtvCac)
                .clientesNuevos(clientesNuevos)
                .totalClientes(totalClientes)
                .tasaConversion(tasaConversion)
                .roiPublicidad(roiPublicidad)
                .gastoMarketing(gastoMarketing)
                .build();
    }

    // ── Helpers ──
    private BigDecimal calcularMargenBruto(BigDecimal ventas, BigDecimal costos) {
        if (ventas == null || ventas.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return ventas.subtract(costos)
                .divide(ventas, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal coalesce(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private Long coalesceL(Long v) { return v != null ? v : 0L; }
}