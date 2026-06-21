package com.easymype.backend.repository;

import com.easymype.backend.entity.AlertaInventario;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.TipoAlerta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlertaInventarioRepositoryTest {

    @Autowired
    private AlertaInventarioRepository alertaInventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldFindAlertsByEmpresaIdOrderByCreatedAtDesc")
    void shouldFindAlertsByEmpresaIdOrderByCreatedAtDesc() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        Producto producto = crearProducto("Mouse", "SKU-001", empresa);
        Producto otroProducto = crearProducto("Teclado", "SKU-002", otraEmpresa);

        alertaInventarioRepository.save(crearAlerta(producto, empresa, TipoAlerta.STOCK_BAJO, "Stock bajo"));
        alertaInventarioRepository.save(crearAlerta(otroProducto, otraEmpresa, TipoAlerta.STOCK_AGOTADO, "Stock agotado"));

        List<AlertaInventario> result = alertaInventarioRepository.findByEmpresaIdOrderByCreatedAtDesc(empresa.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmpresa().getId()).isEqualTo(empresa.getId());
        assertThat(result.get(0).getMensaje()).isEqualTo("Stock bajo");
    }

    @Test
    @DisplayName("shouldFindAlertByIdAndEmpresaIdWhenAlertBelongsToEmpresa")
    void shouldFindAlertByIdAndEmpresaIdWhenAlertBelongsToEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Producto producto = crearProducto("Mouse", "SKU-001", empresa);

        AlertaInventario alerta = alertaInventarioRepository.save(
                crearAlerta(producto, empresa, TipoAlerta.STOCK_BAJO, "Stock bajo")
        );

        Optional<AlertaInventario> result = alertaInventarioRepository.findByIdAndEmpresaId(
                alerta.getId(),
                empresa.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getMensaje()).isEqualTo("Stock bajo");
        assertThat(result.get().getTipo()).isEqualTo(TipoAlerta.STOCK_BAJO);
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenAlertBelongsToAnotherEmpresa")
    void shouldReturnEmptyWhenAlertBelongsToAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        Producto producto = crearProducto("Mouse", "SKU-001", empresa);

        AlertaInventario alerta = alertaInventarioRepository.save(
                crearAlerta(producto, empresa, TipoAlerta.STOCK_BAJO, "Stock bajo")
        );

        Optional<AlertaInventario> result = alertaInventarioRepository.findByIdAndEmpresaId(
                alerta.getId(),
                otraEmpresa.getId()
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldSaveAlertAsUnreadByDefault")
    void shouldSaveAlertAsUnreadByDefault() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Producto producto = crearProducto("Mouse", "SKU-001", empresa);

        AlertaInventario alerta = alertaInventarioRepository.save(
                crearAlerta(producto, empresa, TipoAlerta.STOCK_BAJO, "Stock bajo")
        );

        assertThat(alerta.getId()).isNotNull();
        assertThat(alerta.isLeida()).isFalse();
    }

    private Empresa crearEmpresa(String nombre, String ruc) {
        Empresa empresa = Empresa.builder()
                .nombre(nombre)
                .ruc(ruc)
                .build();

        return empresaRepository.save(empresa);
    }

    private Producto crearProducto(String nombre, String sku, Empresa empresa) {
        Producto producto = Producto.builder()
                .nombre(nombre)
                .descripcion("Producto de prueba")
                .sku(sku)
                .stockActual(2)
                .stockMinimo(5)
                .precioVenta(new BigDecimal("100.00"))
                .costo(new BigDecimal("70.00"))
                .estadoStock(EstadoStock.BAJO)
                .empresa(empresa)
                .build();

        return productoRepository.save(producto);
    }

    private AlertaInventario crearAlerta(
            Producto producto,
            Empresa empresa,
            TipoAlerta tipo,
            String mensaje
    ) {
        return AlertaInventario.builder()
                .producto(producto)
                .empresa(empresa)
                .tipo(tipo)
                .mensaje(mensaje)
                .build();
    }
}