package com.easymype.backend.service;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        YearMonth mesActual = YearMonth.now();
        LocalDateTime inicio = mesActual.atDay(1).atStartOfDay();
        LocalDateTime fin = mesActual.atEndOfMonth().atTime(23, 59, 59);

        long totalProductos = productoRepository.countByEmpresaId(empresaId);
        long stockBajo = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.BAJO);
        long agotados = productoRepository.countByEmpresaIdAndEstadoStock(empresaId, EstadoStock.AGOTADO);
        long ventasDelMes = ventaRepository.countByEmpresaIdAndFechaBetween(empresaId, inicio, fin);
        BigDecimal ingresos = ventaRepository.sumTotalByEmpresaIdAndFechaBetween(empresaId, inicio, fin);

        return DashboardSummaryDTO.builder()
                .totalProductos(totalProductos)
                .productosStockBajo(stockBajo)
                .productosAgotados(agotados)
                .ventasDelMes(ventasDelMes)
                .ingresosDelMes(ingresos != null ? ingresos : BigDecimal.ZERO)
                .build();
    }
}
