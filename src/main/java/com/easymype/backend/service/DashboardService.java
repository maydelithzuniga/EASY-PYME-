package com.easymype.backend.service;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.dto.dashboard.ProductoAlertaDTO;
import com.easymype.backend.dto.dashboard.ProductoTopVentaDTO;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.TipoPeriodo;
import com.easymype.backend.entity.Usuario;
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

    private TipoPeriodo detectarTipoPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        LocalDate desde = inicio.toLocalDate();
        LocalDate hasta = fin.toLocalDate();

        // Mensual: desde es día 1 del mes, hasta es el último día del mismo mes
        boolean esMensual = desde.equals(desde.withDayOfMonth(1))
                && hasta.equals(desde.withDayOfMonth(desde.lengthOfMonth()))
                && desde.getMonth() == hasta.getMonth()
                && desde.getYear() == hasta.getYear();

        if (esMensual) {
            return TipoPeriodo.MENSUAL;
        }

        // Trimestral: desde es el primer día de un trimestre, hasta el último día del 3er mes de ese trimestre
        int mesInicioTrimestre = ((desde.getMonthValue() - 1) / 3) * 3 + 1;
        LocalDate inicioTrimestreEsperado = LocalDate.of(desde.getYear(), mesInicioTrimestre, 1);
        LocalDate finTrimestreEsperado = inicioTrimestreEsperado.plusMonths(3).minusDays(1);

        boolean esTrimestral = desde.equals(inicioTrimestreEsperado) && hasta.equals(finTrimestreEsperado);

        return esTrimestral ? TipoPeriodo.TRIMESTRAL : TipoPeriodo.PERSONALIZADO;
    }

    private LocalDateTime[] calcularPeriodoAnterior(LocalDateTime inicio, LocalDateTime fin, TipoPeriodo tipo) {
        LocalDate desde = inicio.toLocalDate();
        LocalDate hasta = fin.toLocalDate();

        return switch (tipo) {
            case MENSUAL -> {
                YearMonth mesAnterior = YearMonth.from(desde).minusMonths(1);
                yield new LocalDateTime[]{
                        mesAnterior.atDay(1).atStartOfDay(),
                        mesAnterior.atEndOfMonth().atTime(23, 59, 59)
                };
            }
            case TRIMESTRAL -> {
                LocalDate inicioAnterior = desde.minusMonths(3);
                LocalDate finAnterior = inicioAnterior.plusMonths(3).minusDays(1);
                yield new LocalDateTime[]{
                        inicioAnterior.atStartOfDay(),
                        finAnterior.atTime(23, 59, 59)
                };
            }
            case PERSONALIZADO -> {
                // Fallback: mismo tamaño de rango, desplazado hacia atrás
                long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
                yield new LocalDateTime[]{
                        desde.minusDays(dias).atStartOfDay(),
                        desde.minusDays(1).atTime(23, 59, 59)
                };
            }
        };
    }

    private BigDecimal calcularVariacionPorcentual(BigDecimal actual, BigDecimal anterior) {
        actual = actual != null ? actual : BigDecimal.ZERO;
        anterior = anterior != null ? anterior : BigDecimal.ZERO;

        if (anterior.compareTo(BigDecimal.ZERO) == 0) {

            return actual.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(100); // o null, según cómo quieras representarlo en el front
        }

        return actual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }


    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(Usuario usuario, LocalDateTime desde, LocalDateTime hasta) { {
        Long empresaId = usuario.getEmpresa().getId();

        if (desde==null || hasta==null){
            YearMonth mesActual = YearMonth.now();
            desde = mesActual.atDay(1).atStartOfDay();
            hasta = mesActual.atEndOfMonth().atTime(23, 59, 59);
        }

        TipoPeriodo tipo = detectarTipoPeriodo(desde, hasta);
        LocalDateTime[] periodoAnterior = calcularPeriodoAnterior(desde, hasta, tipo);

        LocalDateTime inicioAnterior = periodoAnterior[0];
        LocalDateTime finAnterior = periodoAnterior[1];

        BigDecimal ingresosPeriodoAnterior = ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicioAnterior, finAnterior);
        long ventasPeriodoAnterior = ventaRepository.countByEmpresaIdAndFechaBetween(empresaId, inicioAnterior, finAnterior);
        BigDecimal ingresos = ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, desde, hasta);

        BigDecimal variacionIngresos = calcularVariacionPorcentual(ingresos, ingresosPeriodoAnterior);
        BigDecimal valorInventario = productoRepository.sumValorInventarioByEmpresaId(empresaId); // SUM(stock * precio)
        List<ProductoTopVentaDTO> topProductosVendidos = ventaRepository.findTopProductosByEmpresaId(empresaId, desde, hasta, PageRequest.of(0, 5));

        List<ProductoAlertaDTO> productosAgotados = productoRepository.findTopByEmpresaIdAndEstadoStock(
                        empresaId, EstadoStock.AGOTADO, PageRequest.of(0, 5))
                .stream()
                .map(p -> ProductoAlertaDTO.builder()
                        .productoId(p.getProductoId())
                        .nombre(p.getNombre())
                        .sku(p.getSku())
                        .stockActual(p.getStockActual())
                        .stockMinimo(p.getStockMinimo())
                        .estadoStock(p.getEstadoStock())
                        .build())
                .toList();

        long totalProductos = productoRepository.countByEmpresaId(empresaId);
        long stockBajo = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.BAJO);
        long agotados = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.AGOTADO);
        long ventasDelPeriodoActual = ventaRepository.countByEmpresaIdAndFechaBetween(empresaId, desde, hasta);

        return DashboardSummaryDTO.builder()
                .totalProductos(totalProductos)
                .productosStockBajo(stockBajo)
                .productosAgotados(agotados)
                .VentasDelPeriodoActual(ventasDelPeriodoActual)
                .ingresosDelMes(ingresos != null ? ingresos : BigDecimal.ZERO)
                .build();
    }


}