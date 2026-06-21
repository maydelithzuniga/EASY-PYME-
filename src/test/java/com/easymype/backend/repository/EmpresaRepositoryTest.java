package com.easymype.backend.repository;

import com.easymype.backend.entity.Empresa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldSaveEmpresaWhenEmpresaIsValid")
    void shouldSaveEmpresaWhenEmpresaIsValid() {
        Empresa empresa = Empresa.builder()
                .nombre("Empresa Test")
                .ruc("12345678901")
                .build();

        Empresa saved = empresaRepository.save(empresa);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("Empresa Test");
        assertThat(saved.getRuc()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("shouldReturnTrueWhenRucExists")
    void shouldReturnTrueWhenRucExists() {
        Empresa empresa = Empresa.builder()
                .nombre("Empresa Test")
                .ruc("12345678901")
                .build();

        empresaRepository.save(empresa);

        boolean exists = empresaRepository.existsByRuc("12345678901");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("shouldReturnFalseWhenRucDoesNotExist")
    void shouldReturnFalseWhenRucDoesNotExist() {
        boolean exists = empresaRepository.existsByRuc("00000000000");

        assertThat(exists).isFalse();
    }
}