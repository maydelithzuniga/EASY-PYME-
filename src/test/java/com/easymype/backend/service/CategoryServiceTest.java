package com.easymype.backend.service;

import com.easymype.backend.dto.category.CategoryRequestDTO;
import com.easymype.backend.dto.category.CategoryResponseDTO;
import com.easymype.backend.entity.Categoria;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("shouldCreateCategoryWhenNameIsAvailable")
    void shouldCreateCategoryWhenNameIsAvailable() {
        Usuario usuario = crearUsuario();
        CategoryRequestDTO request = crearRequest("Tecnología");

        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnología")
                .empresa(usuario.getEmpresa())
                .build();

        when(categoriaRepository.existsByNombreAndEmpresaId("Tecnología", 1L)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoryResponseDTO result = categoryService.create(request, usuario);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Tecnología");
        assertThat(result.getEmpresaId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenCategoryNameAlreadyExists")
    void shouldThrowConflictExceptionWhenCategoryNameAlreadyExists() {
        Usuario usuario = crearUsuario();
        CategoryRequestDTO request = crearRequest("Tecnología");

        when(categoriaRepository.existsByNombreAndEmpresaId("Tecnología", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.create(request, usuario));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("shouldFindAllCategoriesByEmpresa")
    void shouldFindAllCategoriesByEmpresa() {
        Usuario usuario = crearUsuario();

        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnología")
                .empresa(usuario.getEmpresa())
                .build();

        when(categoriaRepository.findByEmpresaId(1L)).thenReturn(List.of(categoria));

        List<CategoryResponseDTO> result = categoryService.findAll(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tecnología");
    }

    @Test
    @DisplayName("shouldUpdateCategoryWhenCategoryExists")
    void shouldUpdateCategoryWhenCategoryExists() {
        Usuario usuario = crearUsuario();
        CategoryRequestDTO request = crearRequest("Nueva categoría");

        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnología")
                .empresa(usuario.getEmpresa())
                .build();

        when(categoriaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoryResponseDTO result = categoryService.update(1L, request, usuario);

        assertThat(result.getNombre()).isEqualTo("Nueva categoría");
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenUpdatingMissingCategory")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingMissingCategory() {
        Usuario usuario = crearUsuario();
        CategoryRequestDTO request = crearRequest("Nueva categoría");

        when(categoriaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(99L, request, usuario));
    }

    @Test
    @DisplayName("shouldDeleteCategoryWhenCategoryExists")
    void shouldDeleteCategoryWhenCategoryExists() {
        Usuario usuario = crearUsuario();

        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnología")
                .empresa(usuario.getEmpresa())
                .build();

        when(categoriaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(categoria));

        categoryService.delete(1L, usuario);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingMissingCategory")
    void shouldThrowResourceNotFoundExceptionWhenDeletingMissingCategory() {
        Usuario usuario = crearUsuario();

        when(categoriaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(99L, usuario));
    }

    private CategoryRequestDTO crearRequest(String nombre) {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setNombre(nombre);
        return request;
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