package com.easymype.backend.repository;

import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldFindUserByUsernameWhenUsernameExists")
    void shouldFindUserByUsernameWhenUsernameExists() {
        Empresa empresa = crearEmpresa();
        usuarioRepository.save(crearUsuario("luis", "luis@test.com", empresa));

        Optional<Usuario> result = usuarioRepository.findByUsername("luis");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("luis");
    }

    @Test
    @DisplayName("shouldFindUserByEmailWhenEmailExists")
    void shouldFindUserByEmailWhenEmailExists() {
        Empresa empresa = crearEmpresa();
        usuarioRepository.save(crearUsuario("luis", "luis@test.com", empresa));

        Optional<Usuario> result = usuarioRepository.findByEmail("luis@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("luis@test.com");
    }

    @Test
    @DisplayName("shouldReturnTrueWhenUsernameExists")
    void shouldReturnTrueWhenUsernameExists() {
        Empresa empresa = crearEmpresa();
        usuarioRepository.save(crearUsuario("luis", "luis@test.com", empresa));

        boolean exists = usuarioRepository.existsByUsername("luis");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("shouldReturnTrueWhenEmailExists")
    void shouldReturnTrueWhenEmailExists() {
        Empresa empresa = crearEmpresa();
        usuarioRepository.save(crearUsuario("luis", "luis@test.com", empresa));

        boolean exists = usuarioRepository.existsByEmail("luis@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenUsernameDoesNotExist")
    void shouldReturnEmptyWhenUsernameDoesNotExist() {
        Optional<Usuario> result = usuarioRepository.findByUsername("noexiste");

        assertThat(result).isEmpty();
    }

    private Empresa crearEmpresa() {
        Empresa empresa = Empresa.builder()
                .nombre("Empresa Test")
                .ruc("12345678901")
                .build();

        return empresaRepository.save(empresa);
    }

    private Usuario crearUsuario(String username, String email, Empresa empresa) {
        return Usuario.builder()
                .username(username)
                .email(email)
                .passwordHash("hashedPassword")
                .firstName("Luis")
                .lastName("Sanchez")
                .role(Role.USER)
                .empresa(empresa)
                .enabled(true)
                .build();
    }
}