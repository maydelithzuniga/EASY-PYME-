package com.easymype.backend.service;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("shouldReturnDashboardSummaryWhenDataExists")
    void shouldReturnDashboardSummaryWhenDataExists() {
        Usuario usuario = crearUsuario();

        when(productoRepository.countByEmpresaId(1L)).thenReturn(20L);
        when(productoRepository.countByEmpresaIdAndEstadoStock(1L, EstadoStock.BAJO)).thenReturn(3L);
        when(productoRepository.countByEmpresaIdAndEstadoStock(1L, EstadoStock.AGOTADO)).thenReturn(1L);
        when(ventaRepository.countByEmpresaIdAndFechaBetween(
                eqLong(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(5L);
        when(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(
                eqLong(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("1500.00"));

        DashboardSummaryDTO result = dashboardService.getSummary(usuario);

        assertThat(result.getTotalProductos()).isEqualTo(20L);
        assertThat(result.getProductosStockBajo()).isEqualTo(3L);
        assertThat(result.getProductosAgotados()).isEqualTo(1L);
        assertThat(result.getVentasDelMes()).isEqualTo(5L);
        assertThat(result.getIngresosDelMes()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("shouldReturnZeroIncomeWhenRepositoryReturnsNull")
    void shouldReturnZeroIncomeWhenRepositoryReturnsNull() {
        Usuario usuario = crearUsuario();

        when(productoRepository.countByEmpresaId(1L)).thenReturn(0L);
        when(productoRepository.countByEmpresaIdAndEstadoStock(1L, EstadoStock.BAJO)).thenReturn(0L);
        when(productoRepository.countByEmpresaIdAndEstadoStock(1L, EstadoStock.AGOTADO)).thenReturn(0L);
        when(ventaRepository.countByEmpresaIdAndFechaBetween(
                eqLong(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(0L);
        when(ventaRepository.sumTotalByEmpresaIdAndFechaBetween(
                eqLong(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(null);

        DashboardSummaryDTO result = dashboardService.getSummary(usuario);

        assertThat(result.getIngresosDelMes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Long eqLong(Long value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    private Usuario crearUsuario() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .nombre("Empresa Test")
                .build();

        return Usuario.builder()
                .id(1L)
                .username("luis")
                .empresa(empresa)
                .build();
    }
}