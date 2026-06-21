package com.easymype.backend.service;

import com.easymype.backend.dto.category.CategoryRequestDTO;
import com.easymype.backend.dto.category.CategoryResponseDTO;
import com.easymype.backend.entity.Categoria;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        if (categoriaRepository.existsByNombreAndEmpresaId(request.getNombre(), empresaId)) {
            throw new ConflictException("Ya existe una categoría con nombre '" + request.getNombre() + "'");
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .empresa(usuario.getEmpresa())
                .build();

        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll(Usuario usuario) {
        return categoriaRepository.findByEmpresaId(usuario.getEmpresa().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        Categoria categoria = categoriaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con id=" + id + " no encontrada"));

        categoria.setNombre(request.getNombre());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long id, Usuario usuario) {
        Categoria categoria = categoriaRepository.findByIdAndEmpresaId(id, usuario.getEmpresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con id=" + id + " no encontrada"));
        categoriaRepository.delete(categoria);
    }

    private CategoryResponseDTO toResponse(Categoria c) {
        return CategoryResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .empresaId(c.getEmpresa().getId())
                .build();
    }
}
