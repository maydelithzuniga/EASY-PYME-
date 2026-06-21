package com.easymype.backend.repository;

import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.EstadoStock;
import com.easymype.backend.entity.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldSaveProductWhenProductIsValid")
    void shouldSaveProductWhenProductIsValid() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        Producto producto = Producto.builder()
                .nombre("Laptop")
                .descripcion("Laptop Lenovo")
                .sku("SKU-001")
                .stockActual(10)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("2500.00"))
                .costo(new BigDecimal("2000.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresa(empresa)
                .build();

        Producto saved = productoRepository.save(producto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("Laptop");
        assertThat(saved.getSku()).isEqualTo("SKU-001");
        assertThat(saved.getEmpresa().getId()).isEqualTo(empresa.getId());
    }

    @Test
    @DisplayName("shouldFindProductsByEmpresaIdWhenEmpresaExists")
    void shouldFindProductsByEmpresaIdWhenEmpresaExists() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        productoRepository.save(crearProducto("Mouse", "SKU-001", empresa));
        productoRepository.save(crearProducto("Teclado", "SKU-002", otraEmpresa));

        List<Producto> productos = productoRepository.findByEmpresaId(empresa.getId());

        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getNombre()).isEqualTo("Mouse");
    }

    @Test
    @DisplayName("shouldFindProductByIdAndEmpresaIdWhenProductBelongsToEmpresa")
    void shouldFindProductByIdAndEmpresaIdWhenProductBelongsToEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Producto producto = productoRepository.save(crearProducto("Monitor", "SKU-001", empresa));

        Optional<Producto> result = productoRepository.findByIdAndEmpresaId(producto.getId(), empresa.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenProductDoesNotBelongToEmpresa")
    void shouldReturnEmptyWhenProductDoesNotBelongToEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        Producto producto = productoRepository.save(crearProducto("Monitor", "SKU-001", empresa));

        Optional<Producto> result = productoRepository.findByIdAndEmpresaId(producto.getId(), otraEmpresa.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnTrueWhenSkuExistsInEmpresa")
    void shouldReturnTrueWhenSkuExistsInEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        productoRepository.save(crearProducto("Mouse", "SKU-001", empresa));

        boolean exists = productoRepository.existsBySkuAndEmpresaId("SKU-001", empresa.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("shouldReturnFalseWhenSkuExistsInAnotherEmpresa")
    void shouldReturnFalseWhenSkuExistsInAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        productoRepository.save(crearProducto("Mouse", "SKU-001", empresa));

        boolean exists = productoRepository.existsBySkuAndEmpresaId("SKU-001", otraEmpresa.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("shouldFindProductsByEmpresaIdAndEstadoStock")
    void shouldFindProductsByEmpresaIdAndEstadoStock() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        Producto bajo = crearProducto("Mouse", "SKU-001", empresa);
        bajo.setStockActual(1);
        bajo.setStockMinimo(5);
        bajo.setEstadoStock(EstadoStock.BAJO);

        Producto disponible = crearProducto("Laptop", "SKU-002", empresa);
        disponible.setEstadoStock(EstadoStock.DISPONIBLE);

        productoRepository.save(bajo);
        productoRepository.save(disponible);

        List<Producto> result = productoRepository.findByEmpresaIdAndEstadoStock(empresa.getId(), EstadoStock.BAJO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstadoStock()).isEqualTo(EstadoStock.BAJO);
    }

    @Test
    @DisplayName("shouldCountProductsByEmpresaId")
    void shouldCountProductsByEmpresaId() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        productoRepository.save(crearProducto("Mouse", "SKU-001", empresa));
        productoRepository.save(crearProducto("Teclado", "SKU-002", empresa));
        productoRepository.save(crearProducto("Monitor", "SKU-003", otraEmpresa));

        long count = productoRepository.countByEmpresaId(empresa.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("shouldCountProductsByEmpresaIdAndEstadoStock")
    void shouldCountProductsByEmpresaIdAndEstadoStock() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        Producto bajo = crearProducto("Mouse", "SKU-001", empresa);
        bajo.setEstadoStock(EstadoStock.BAJO);

        Producto agotado = crearProducto("Teclado", "SKU-002", empresa);
        agotado.setEstadoStock(EstadoStock.AGOTADO);

        productoRepository.save(bajo);
        productoRepository.save(agotado);

        long count = productoRepository.countByEmpresaIdAndEstadoStock(empresa.getId(), EstadoStock.BAJO);

        assertThat(count).isEqualTo(1);
    }

    private Empresa crearEmpresa(String nombre, String ruc) {
        Empresa empresa = Empresa.builder()
                .nombre(nombre)
                .ruc(ruc)
                .build();

        return empresaRepository.save(empresa);
    }

    private Producto crearProducto(String nombre, String sku, Empresa empresa) {
        return Producto.builder()
                .nombre(nombre)
                .descripcion("Producto de prueba")
                .sku(sku)
                .stockActual(10)
                .stockMinimo(2)
                .precioVenta(new BigDecimal("100.00"))
                .costo(new BigDecimal("70.00"))
                .estadoStock(EstadoStock.DISPONIBLE)
                .empresa(empresa)
                .build();
    }
}