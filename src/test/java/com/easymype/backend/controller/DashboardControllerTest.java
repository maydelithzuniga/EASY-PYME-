package com.easymype.backend.controller;

import com.easymype.backend.dto.dashboard.DashboardSummaryDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        Empresa empresa = Empresa.builder().id(1L).nombre("Empresa Test").build();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .role(Role.ADMIN)
                .empresa(empresa)
                .enabled(true)
                .build();
        authToken = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }

    @Test
    @DisplayName("shouldReturn200WithDashboardSummary")
    void shouldReturn200WithDashboardSummary() throws Exception {
        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalProductos(10L)
                .productosStockBajo(2L)
                .productosAgotados(1L)
                .ventasDelMes(5L)
                .ingresosDelMes(new BigDecimal("1500.00"))
                .build();

        when(dashboardService.getSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(10))
                .andExpect(jsonPath("$.productosStockBajo").value(2))
                .andExpect(jsonPath("$.productosAgotados").value(1))
                .andExpect(jsonPath("$.ventasDelMes").value(5))
                .andExpect(jsonPath("$.ingresosDelMes").value(1500.00));
    }

}
