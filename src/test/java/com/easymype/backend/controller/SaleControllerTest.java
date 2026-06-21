package com.easymype.backend.controller;

import com.easymype.backend.dto.sale.SaleItemRequestDTO;
import com.easymype.backend.dto.sale.SaleRequestDTO;
import com.easymype.backend.dto.sale.SaleResponseDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.InsufficientStockException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.SaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authToken;
    private SaleResponseDTO ventaResponse;

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

        ventaResponse = SaleResponseDTO.builder()
                .id(1L)
                .total(new BigDecimal("250.00"))
                .fecha(LocalDateTime.now())
                .empresaId(1L)
                .build();
    }

    @Test
    @DisplayName("shouldReturn201WhenSaleIsRegistered")
    void shouldReturn201WhenSaleIsRegistered() throws Exception {
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductoId(1L);
        item.setCantidad(2);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setItems(List.of(item));

        when(saleService.registrarVenta(any(SaleRequestDTO.class), any())).thenReturn(ventaResponse);

        mockMvc.perform(post("/api/v1/sales")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.total").value(250.00));
    }

    @Test
    @DisplayName("shouldReturn400WhenStockIsInsufficient")
    void shouldReturn400WhenStockIsInsufficient() throws Exception {
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductoId(1L);
        item.setCantidad(999);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setItems(List.of(item));

        when(saleService.registrarVenta(any(SaleRequestDTO.class), any()))
                .thenThrow(new InsufficientStockException("Laptop", 999, 5));

        mockMvc.perform(post("/api/v1/sales")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn200WithSaleListWhenFindAll")
    void shouldReturn200WithSaleListWhenFindAll() throws Exception {
        when(saleService.findAll(any())).thenReturn(List.of(ventaResponse));

        mockMvc.perform(get("/api/v1/sales")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("shouldReturn200WhenSaleFoundById")
    void shouldReturn200WhenSaleFoundById() throws Exception {
        when(saleService.findById(eq(1L), any())).thenReturn(ventaResponse);

        mockMvc.perform(get("/api/v1/sales/1")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("shouldReturn404WhenSaleNotFound")
    void shouldReturn404WhenSaleNotFound() throws Exception {
        when(saleService.findById(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Venta con id=99 no encontrada"));

        mockMvc.perform(get("/api/v1/sales/99")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound());
    }
}
