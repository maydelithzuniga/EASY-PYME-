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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El username '" + request.getUsername() + "' ya está en uso");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email '" + request.getEmail() + "' ya está en uso");
        }

        Empresa empresa = Empresa.builder()
                .nombre(request.getEmpresaNombre())
                .build();
        empresa = empresaRepository.save(empresa);

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ADMIN)
                .empresa(empresa)
                .enabled(true)
                .build();
        usuarioRepository.save(usuario);
        emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getFirstName(), empresa.getNombre());

        String token = jwtService.generateToken(usuario);
        return buildAuthResponse(usuario, token);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(usuario);
        return buildAuthResponse(usuario, token);
    }

    private AuthResponseDTO buildAuthResponse(Usuario usuario, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .empresaId(usuario.getEmpresa().getId())
                .build();
    }
}
