package com.easymype.backend.service;

import com.easymype.backend.dto.auth.AuthResponseDTO;
import com.easymype.backend.dto.auth.LoginRequestDTO;
import com.easymype.backend.dto.auth.RegisterRequestDTO;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ConflictException;
import com.easymype.backend.repository.EmpresaRepository;
import com.easymype.backend.repository.UsuarioRepository;
import com.easymype.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("shouldRegisterUserWhenUsernameAndEmailAreAvailable")
    void shouldRegisterUserWhenUsernameAndEmailAreAvailable() {
        RegisterRequestDTO request = crearRegisterRequest();

        when(usuarioRepository.existsByUsername("luis")).thenReturn(false);
        when(usuarioRepository.existsByEmail("luis@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(1L);
            return empresa;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("jwt-token");
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString(), anyString());

        AuthResponseDTO result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUsername()).isEqualTo("luis");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getEmpresaId()).isEqualTo(1L);

        verify(empresaRepository).save(any(Empresa.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(passwordEncoder).encode("Password123");
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenUsernameAlreadyExists")
    void shouldThrowConflictExceptionWhenUsernameAlreadyExists() {
        RegisterRequestDTO request = crearRegisterRequest();

        when(usuarioRepository.existsByUsername("luis")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("shouldThrowConflictExceptionWhenEmailAlreadyExists")
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        RegisterRequestDTO request = crearRegisterRequest();

        when(usuarioRepository.existsByUsername("luis")).thenReturn(false);
        when(usuarioRepository.existsByEmail("luis@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("shouldLoginUserWhenCredentialsAreValid")
    void shouldLoginUserWhenCredentialsAreValid() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("luis");
        request.setPassword("Password123");

        Usuario usuario = crearUsuario();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUsername()).isEqualTo("luis");
        assertThat(result.getEmail()).isEqualTo("luis@test.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(usuario);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenLoginUserDoesNotExist")
    void shouldThrowExceptionWhenLoginUserDoesNotExist() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("luis");
        request.setPassword("Password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    private RegisterRequestDTO crearRegisterRequest() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("luis");
        request.setEmail("luis@test.com");
        request.setPassword("Password123");
        request.setFirstName("Luis");
        request.setLastName("Sanchez");
        request.setEmpresaNombre("Empresa Test");
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
                .email("luis@test.com")
                .passwordHash("hashedPassword")
                .firstName("Luis")
                .lastName("Sanchez")
                .role(Role.ADMIN)
                .empresa(empresa)
                .enabled(true)
                .build();
    }
}