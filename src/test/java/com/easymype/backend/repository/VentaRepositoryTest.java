package com.easymype.backend.repository;

import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Role;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.entity.Venta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VentaRepositoryTest {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("shouldFindSalesByEmpresaId")
    void shouldFindSalesByEmpresaId() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");
        Usuario usuario = crearUsuario("luis", "luis@test.com", empresa);
        Usuario otroUsuario = crearUsuario("ana", "ana@test.com", otraEmpresa);

        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("100.00"), LocalDateTime.now()));
        ventaRepository.save(crearVenta(otraEmpresa, otroUsuario, new BigDecimal("50.00"), LocalDateTime.now()));

        List<Venta> result = ventaRepository.findByEmpresaId(empresa.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmpresa().getId()).isEqualTo(empresa.getId());
    }

    @Test
    @DisplayName("shouldFindSaleByIdAndEmpresaIdWhenSaleBelongsToEmpresa")
    void shouldFindSaleByIdAndEmpresaIdWhenSaleBelongsToEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Usuario usuario = crearUsuario("luis", "luis@test.com", empresa);

        Venta venta = ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("100.00"), LocalDateTime.now()));

        Optional<Venta> result = ventaRepository.findByIdAndEmpresaId(venta.getId(), empresa.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenSaleBelongsToAnotherEmpresa")
    void shouldReturnEmptyWhenSaleBelongsToAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");
        Usuario usuario = crearUsuario("luis", "luis@test.com", empresa);

        Venta venta = ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("100.00"), LocalDateTime.now()));

        Optional<Venta> result = ventaRepository.findByIdAndEmpresaId(venta.getId(), otraEmpresa.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldCountSalesBetweenDates")
    void shouldCountSalesBetweenDates() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Usuario usuario = crearUsuario("luis", "luis@test.com", empresa);

        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("100.00"), LocalDateTime.of(2026, 5, 10, 10, 0)));
        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("50.00"), LocalDateTime.of(2026, 5, 15, 10, 0)));
        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("30.00"), LocalDateTime.of(2026, 6, 1, 10, 0)));

        long count = ventaRepository.countByEmpresaIdAndFechaBetween(
                empresa.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("shouldSumTotalSalesBetweenDates")
    void shouldSumTotalSalesBetweenDates() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Usuario usuario = crearUsuario("luis", "luis@test.com", empresa);

        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("100.00"), LocalDateTime.of(2026, 5, 10, 10, 0)));
        ventaRepository.save(crearVenta(empresa, usuario, new BigDecimal("50.00"), LocalDateTime.of(2026, 5, 15, 10, 0)));

        BigDecimal total = ventaRepository.sumTotalByEmpresaIdAndFechaBetween(
                empresa.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(total).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("shouldReturnZeroWhenNoSalesBetweenDates")
    void shouldReturnZeroWhenNoSalesBetweenDates() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        BigDecimal total = ventaRepository.sumTotalByEmpresaIdAndFechaBetween(
                empresa.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(total).isEqualByComparingTo("0");
    }

    private Empresa crearEmpresa(String nombre, String ruc) {
        Empresa empresa = Empresa.builder()
                .nombre(nombre)
                .ruc(ruc)
                .build();

        return empresaRepository.save(empresa);
    }

    private Usuario crearUsuario(String username, String email, Empresa empresa) {
        Usuario usuario = Usuario.builder()
                .username(username)
                .email(email)
                .passwordHash("hashedPassword")
                .firstName("Luis")
                .lastName("Sanchez")
                .role(Role.USER)
                .empresa(empresa)
                .enabled(true)
                .build();

        return usuarioRepository.save(usuario);
    }

    private Venta crearVenta(Empresa empresa, Usuario usuario, BigDecimal total, LocalDateTime fecha) {
        return Venta.builder()
                .empresa(empresa)
                .usuario(usuario)
                .fecha(fecha)
                .total(total)
                .build();
    }
}