package com.easymype.backend.repository;

import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Proyecto;
import com.easymype.backend.entity.TablaInventario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TablaInventarioRepositoryTest {

    @Autowired
    private TablaInventarioRepository tablaInventarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("shouldFindTablesByEmpresaId")
    void shouldFindTablesByEmpresaId() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        tablaInventarioRepository.save(crearTabla("Inventario Principal", empresa));
        tablaInventarioRepository.save(crearTabla("Inventario Secundario", otraEmpresa));

        List<TablaInventario> result = tablaInventarioRepository.findByEmpresaId(empresa.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Inventario Principal");
        assertThat(result.get(0).getEmpresa().getId()).isEqualTo(empresa.getId());
    }

    @Test
    @DisplayName("shouldFindTableByIdAndEmpresaIdWhenTableBelongsToEmpresa")
    void shouldFindTableByIdAndEmpresaIdWhenTableBelongsToEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        TablaInventario tabla = tablaInventarioRepository.save(
                crearTabla("Inventario Principal", empresa)
        );

        Optional<TablaInventario> result = tablaInventarioRepository.findByIdAndEmpresaId(
                tabla.getId(),
                empresa.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Inventario Principal");
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenTableBelongsToAnotherEmpresa")
    void shouldReturnEmptyWhenTableBelongsToAnotherEmpresa() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");
        Empresa otraEmpresa = crearEmpresa("Otra Empresa", "10987654321");

        TablaInventario tabla = tablaInventarioRepository.save(
                crearTabla("Inventario Principal", empresa)
        );

        Optional<TablaInventario> result = tablaInventarioRepository.findByIdAndEmpresaId(
                tabla.getId(),
                otraEmpresa.getId()
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldSaveTableWithoutProjectWhenProjectIsNull")
    void shouldSaveTableWithoutProjectWhenProjectIsNull() {
        Empresa empresa = crearEmpresa("Empresa Test", "12345678901");

        TablaInventario tabla = tablaInventarioRepository.save(
                crearTabla("Inventario Principal", empresa)
        );

        assertThat(tabla.getId()).isNotNull();
        assertThat(tabla.getProyecto()).isNull();
        assertThat(tabla.getNombre()).isEqualTo("Inventario Principal");
    }

    private Empresa crearEmpresa(String nombre, String ruc) {
        Empresa empresa = Empresa.builder()
                .nombre(nombre)
                .ruc(ruc)
                .build();

        return empresaRepository.save(empresa);
    }

    private TablaInventario crearTabla(String nombre, Empresa empresa) {
        return TablaInventario.builder()
                .nombre(nombre)
                .empresa(empresa)
                .proyecto(null)
                .build();
    }
}