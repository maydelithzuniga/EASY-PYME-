package com.easymype.backend.repository;

import com.easymype.backend.entity.Categoria;
import com.easymype.backend.entity.Empresa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldFindCategoriesByEmpresaId")
    void shouldFindCategoriesByEmpresaId() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        categoriaRepository.save(crearCategoria("Tecnología", empresa));
        categoriaRepository.save(crearCategoria("Comida", otraEmpresa));

        List<Categoria> result = categoriaRepository.findByEmpresaId(empresa.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tecnología");
    }

    @Test
    @DisplayName("shouldFindCategoryByIdAndEmpresaId")
    void shouldFindCategoryByIdAndEmpresaId() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Categoria categoria = categoriaRepository.save(crearCategoria("Tecnología", empresa));

        Optional<Categoria> result = categoriaRepository.findByIdAndEmpresaId(categoria.getId(), empresa.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Tecnología");
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenCategoryBelongsToAnotherEmpresa")
    void shouldReturnEmptyWhenCategoryBelongsToAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        Categoria categoria = categoriaRepository.save(crearCategoria("Tecnología", empresa));

        Optional<Categoria> result = categoriaRepository.findByIdAndEmpresaId(categoria.getId(), otraEmpresa.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnTrueWhenCategoryNameExistsInEmpresa")
    void shouldReturnTrueWhenCategoryNameExistsInEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        categoriaRepository.save(crearCategoria("Tecnología", empresa));

        boolean exists = categoriaRepository.existsByNombreAndEmpresaId("Tecnología", empresa.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("shouldReturnFalseWhenCategoryNameExistsInAnotherEmpresa")
    void shouldReturnFalseWhenCategoryNameExistsInAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        categoriaRepository.save(crearCategoria("Tecnología", empresa));

        boolean exists = categoriaRepository.existsByNombreAndEmpresaId("Tecnología", otraEmpresa.getId());

        assertThat(exists).isFalse();
    }

    private Empresa crearEmpresa(String nombre, String ruc) {
        Empresa empresa = Empresa.builder()
                .nombre(nombre)
                .ruc(ruc)
                .build();

        return empresaRepository.save(empresa);
    }

    private Categoria crearCategoria(String nombre, Empresa empresa) {
        return Categoria.builder()
                .nombre(nombre)
                .empresa(empresa)
                .build();
    }
}