package com.easymype.backend.controller;

import com.easymype.backend.dto.alert.AlertResponseDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.TipoAlerta;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.AlertService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authToken;
    private AlertResponseDTO alertaResponse;

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

        alertaResponse = AlertResponseDTO.builder()
                .id(1L)
                .productoId(1L)
                .productoNombre("Laptop")
                .tipo(TipoAlerta.STOCK_BAJO)
                .mensaje("Stock bajo para Laptop")
                .leida(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("shouldReturn200WithAlertListWhenFindAll")
    void shouldReturn200WithAlertListWhenFindAll() throws Exception {
        when(alertService.findAll(any())).thenReturn(List.of(alertaResponse));

        mockMvc.perform(get("/api/v1/alerts")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productoNombre").value("Laptop"))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    @DisplayName("shouldReturn200WhenAlertIsMarkedAsRead")
    void shouldReturn200WhenAlertIsMarkedAsRead() throws Exception {
        AlertResponseDTO leida = AlertResponseDTO.builder()
                .id(1L).productoId(1L).productoNombre("Laptop")
                .tipo(TipoAlerta.STOCK_BAJO).leida(true).build();

        when(alertService.markAsRead(eq(1L), any())).thenReturn(leida);

        mockMvc.perform(patch("/api/v1/alerts/1/read")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    @DisplayName("shouldReturn404WhenAlertNotFound")
    void shouldReturn404WhenAlertNotFound() throws Exception {
        when(alertService.markAsRead(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Alerta con id=99 no encontrada"));

        mockMvc.perform(patch("/api/v1/alerts/99/read")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldReturn204WhenAlertIsDeleted")
    void shouldReturn204WhenAlertIsDeleted() throws Exception {
        doNothing().when(alertService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/v1/alerts/1")
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());
    }
}
