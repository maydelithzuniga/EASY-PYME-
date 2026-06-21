package com.easymype.backend.controller;

import com.easymype.backend.dto.product.ProductRequestDTO;
import com.easymype.backend.dto.product.ProductResponseDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.ProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authToken;
    private ProductResponseDTO productoResponse;

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

        productoResponse = ProductResponseDTO.builder()
                .id(1L)
                .nombre("Laptop")
                .sku("SKU-001")
                .stockActual(10)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("2500.00"))
                .costo(new BigDecimal("2000.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresaId(1L)
                .build();
    }

    @Test
    @DisplayName("shouldReturn201WhenProductIsCreated")
    void shouldReturn201WhenProductIsCreated() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Laptop");
        request.setSku("SKU-001");
        request.setStockActual(10);
        request.setStockMinimo(2);
        request.setPrecioVenta(new BigDecimal("2500.00"));
        request.setCosto(new BigDecimal("2000.00"));

        when(productService.create(any(ProductRequestDTO.class), any())).thenReturn(productoResponse);

        mockMvc.perform(post("/api/v1/products")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    @DisplayName("shouldReturn200WithProductListWhenFindAll")
    void shouldReturn200WithProductListWhenFindAll() throws Exception {
        when(productService.findAll(any())).thenReturn(List.of(productoResponse));

        mockMvc.perform(get("/api/v1/products")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));
    }

    @Test
    @DisplayName("shouldReturn200WhenProductFoundById")
    void shouldReturn200WhenProductFoundById() throws Exception {
        when(productService.findById(eq(1L), any())).thenReturn(productoResponse);

        mockMvc.perform(get("/api/v1/products/1")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    @DisplayName("shouldReturn404WhenProductNotFound")
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.findById(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Producto con id=99 no encontrado en su empresa"));

        mockMvc.perform(get("/api/v1/products/99")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldReturn200WhenProductIsUpdated")
    void shouldReturn200WhenProductIsUpdated() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Laptop Pro");
        request.setSku("SKU-001");
        request.setStockActual(5);
        request.setStockMinimo(2);
        request.setPrecioVenta(new BigDecimal("3000.00"));
        request.setCosto(new BigDecimal("2500.00"));

        when(productService.update(eq(1L), any(ProductRequestDTO.class), any()))
                .thenReturn(productoResponse);

        mockMvc.perform(put("/api/v1/products/1")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("shouldReturn204WhenProductIsDeleted")
    void shouldReturn204WhenProductIsDeleted() throws Exception {
        doNothing().when(productService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/v1/products/1")
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("shouldReturn200WithLowStockProducts")
    void shouldReturn200WithLowStockProducts() throws Exception {
        ProductResponseDTO bajo = ProductResponseDTO.builder()
                .id(2L).nombre("Mouse").estadoStock(EstadoStock.BAJO).build();

        when(productService.findLowStock(any())).thenReturn(List.of(bajo));

        mockMvc.perform(get("/api/v1/products/low-stock")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoStock").value("BAJO"));
    }

    @Test
    @DisplayName("shouldReturn200WithOutOfStockProducts")
    void shouldReturn200WithOutOfStockProducts() throws Exception {
        ProductResponseDTO agotado = ProductResponseDTO.builder()
                .id(3L).nombre("Teclado").estadoStock(EstadoStock.AGOTADO).build();

        when(productService.findOutOfStock(any())).thenReturn(List.of(agotado));

        mockMvc.perform(get("/api/v1/products/out-of-stock")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoStock").value("AGOTADO"));
    }
}
