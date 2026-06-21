package com.easymype.backend.controller;

import com.easymype.backend.dto.category.CategoryRequestDTO;
import com.easymype.backend.dto.category.CategoryResponseDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.security.JwtService;
import com.easymype.backend.service.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken authToken;
    private CategoryResponseDTO categoriaResponse;

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

        categoriaResponse = CategoryResponseDTO.builder()
                .id(1L)
                .nombre("Electrónica")
                .empresaId(1L)
                .build();
    }

    @Test
    @DisplayName("shouldReturn201WhenCategoryIsCreated")
    void shouldReturn201WhenCategoryIsCreated() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setNombre("Electrónica");

        when(categoryService.create(any(CategoryRequestDTO.class), any())).thenReturn(categoriaResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    @DisplayName("shouldReturn409WhenCategoryNameAlreadyExists")
    void shouldReturn409WhenCategoryNameAlreadyExists() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setNombre("Electrónica");

        when(categoryService.create(any(CategoryRequestDTO.class), any()))
                .thenThrow(new ConflictException("Ya existe una categoría con nombre 'Electrónica'"));

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("shouldReturn200WithCategoryListWhenFindAll")
    void shouldReturn200WithCategoryListWhenFindAll() throws Exception {
        when(categoryService.findAll(any())).thenReturn(List.of(categoriaResponse));

        mockMvc.perform(get("/api/v1/categories")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Electrónica"));
    }

    @Test
    @DisplayName("shouldReturn200WhenCategoryIsUpdated")
    void shouldReturn200WhenCategoryIsUpdated() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setNombre("Tecnología");

        CategoryResponseDTO updated = CategoryResponseDTO.builder()
                .id(1L).nombre("Tecnología").empresaId(1L).build();

        when(categoryService.update(eq(1L), any(CategoryRequestDTO.class), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tecnología"));
    }

    @Test
    @DisplayName("shouldReturn204WhenCategoryIsDeleted")
    void shouldReturn204WhenCategoryIsDeleted() throws Exception {
        doNothing().when(categoryService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/v1/categories/1")
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent());
    }
}
