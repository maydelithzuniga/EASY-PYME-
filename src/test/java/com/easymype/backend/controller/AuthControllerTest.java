package com.easymype.backend.controller;

import com.easymype.backend.dto.auth.AuthResponseDTO;
import com.easymype.backend.dto.auth.LoginRequestDTO;
import com.easymype.backend.dto.auth.RegisterRequestDTO;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("shouldReturnTokenWhenRegisterIsSuccessful")
    void shouldReturnTokenWhenRegisterIsSuccessful() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmpresaNombre("Mi Empresa");

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token")
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .empresaId(1L)
                .build();

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("shouldReturn409WhenUsernameAlreadyExists")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmpresaNombre("Mi Empresa");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new ConflictException("El username 'existinguser' ya está en uso"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("shouldReturnTokenWhenLoginIsSuccessful")
    void shouldReturnTokenWhenLoginIsSuccessful() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token")
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .empresaId(1L)
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
