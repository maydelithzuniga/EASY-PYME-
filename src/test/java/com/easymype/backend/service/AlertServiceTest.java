package com.easymype.backend.service;

import com.easymype.backend.dto.alert.AlertResponseDTO;
import com.easymype.backend.entity.AlertaInventario;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.TipoAlerta;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.AlertMapper;
import com.easymype.backend.repository.AlertaInventarioRepository;
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
class AlertServiceTest {

    @Mock
    private AlertaInventarioRepository alertaRepository;

    @Mock
    private AlertMapper alertMapper;

    @InjectMocks
    private AlertService alertService;

    @Test
    @DisplayName("shouldFindAllAlertsByEmpresa")
    void shouldFindAllAlertsByEmpresa() {
        Usuario usuario = crearUsuario();
        AlertaInventario alerta = crearAlerta();
        AlertResponseDTO response = crearResponse();

        when(alertaRepository.findByEmpresaIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(alerta));
        when(alertMapper.toResponseList(List.of(alerta))).thenReturn(List.of(response));

        List<AlertResponseDTO> result = alertService.findAll(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMensaje()).isEqualTo("Stock bajo");
    }

    @Test
    @DisplayName("shouldMarkAlertAsReadWhenAlertExists")
    void shouldMarkAlertAsReadWhenAlertExists() {
        Usuario usuario = crearUsuario();
        AlertaInventario alerta = crearAlerta();
        AlertResponseDTO response = crearResponse();
        response.setLeida(true);

        when(alertaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(alerta)).thenReturn(alerta);
        when(alertMapper.toResponse(alerta)).thenReturn(response);

        AlertResponseDTO result = alertService.markAsRead(1L, usuario);

        assertThat(alerta.isLeida()).isTrue();
        assertThat(result.isLeida()).isTrue();
        verify(alertaRepository).save(alerta);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenMarkingMissingAlert")
    void shouldThrowResourceNotFoundExceptionWhenMarkingMissingAlert() {
        Usuario usuario = crearUsuario();

        when(alertaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alertService.markAsRead(99L, usuario));
    }

    @Test
    @DisplayName("shouldDeleteAlertWhenAlertExists")
    void shouldDeleteAlertWhenAlertExists() {
        Usuario usuario = crearUsuario();
        AlertaInventario alerta = crearAlerta();

        when(alertaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(alerta));

        alertService.delete(1L, usuario);

        verify(alertaRepository).delete(alerta);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingMissingAlert")
    void shouldThrowResourceNotFoundExceptionWhenDeletingMissingAlert() {
        Usuario usuario = crearUsuario();

        when(alertaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alertService.delete(99L, usuario));
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

    private AlertaInventario crearAlerta() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .nombre("Empresa Test")
                .build();

        Producto producto = Producto.builder()
                .id(1L)
                .nombre("Mouse")
                .empresa(empresa)
                .build();

        return AlertaInventario.builder()
                .id(1L)
                .producto(producto)
                .empresa(empresa)
                .tipo(TipoAlerta.STOCK_BAJO)
                .mensaje("Stock bajo")
                .leida(false)
                .build();
    }

    private AlertResponseDTO crearResponse() {
        return AlertResponseDTO.builder()
                .id(1L)
                .productoId(1L)
                .productoNombre("Mouse")
                .tipo(TipoAlerta.STOCK_BAJO)
                .mensaje("Stock bajo")
                .leida(false)
                .build();
    }
}