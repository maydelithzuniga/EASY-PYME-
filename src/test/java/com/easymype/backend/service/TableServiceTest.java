package com.easymype.backend.service;

import com.easymype.backend.dto.table.CellResponseDTO;
import com.easymype.backend.dto.table.CellUpdateDTO;
import com.easymype.backend.dto.table.ColumnRequestDTO;
import com.easymype.backend.dto.table.ColumnResponseDTO;
import com.easymype.backend.dto.table.RowRequestDTO;
import com.easymype.backend.dto.table.RowResponseDTO;
import com.easymype.backend.dto.table.TableRequestDTO;
import com.easymype.backend.dto.table.TableResponseDTO;
import com.easymype.backend.entity.Celda;
import com.easymype.backend.entity.Columna;
import com.easymype.backend.entity.Empresa;
import com.easymype.backend.entity.Fila;
import com.easymype.backend.entity.Producto;
import com.easymype.backend.entity.TablaInventario;
import com.easymype.backend.entity.Usuario;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.TableMapper;
import com.easymype.backend.repository.CeldaRepository;
import com.easymype.backend.repository.ColumnaRepository;
import com.easymype.backend.repository.FilaRepository;
import com.easymype.backend.repository.ProductoRepository;
import com.easymype.backend.repository.TablaInventarioRepository;
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
class TableServiceTest {

    @Mock
    private TablaInventarioRepository tablaRepository;

    @Mock
    private ColumnaRepository columnaRepository;

    @Mock
    private FilaRepository filaRepository;

    @Mock
    private CeldaRepository celdaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TableMapper tableMapper;

    @InjectMocks
    private TableService tableService;

    @Test
    @DisplayName("shouldCreateTableWhenRequestIsValid")
    void shouldCreateTableWhenRequestIsValid() {
        Usuario usuario = crearUsuario();
        TableRequestDTO request = new TableRequestDTO();
        request.setNombre("Inventario Principal");

        TablaInventario tabla = crearTabla();
        TableResponseDTO response = TableResponseDTO.builder()
                .id(1L)
                .nombre("Inventario Principal")
                .empresaId(1L)
                .build();

        when(tablaRepository.save(any(TablaInventario.class))).thenReturn(tabla);
        when(tableMapper.toResponse(tabla)).thenReturn(response);

        TableResponseDTO result = tableService.create(request, usuario);

        assertThat(result.getNombre()).isEqualTo("Inventario Principal");
        verify(tablaRepository).save(any(TablaInventario.class));
    }

    @Test
    @DisplayName("shouldFindAllTablesByEmpresa")
    void shouldFindAllTablesByEmpresa() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();
        TableResponseDTO response = TableResponseDTO.builder()
                .id(1L)
                .nombre("Inventario Principal")
                .empresaId(1L)
                .build();

        when(tablaRepository.findByEmpresaId(1L)).thenReturn(List.of(tabla));
        when(tableMapper.toResponseList(List.of(tabla))).thenReturn(List.of(response));

        List<TableResponseDTO> result = tableService.findAll(usuario);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Inventario Principal");
    }

    @Test
    @DisplayName("shouldFindTableByIdWhenBelongsToEmpresa")
    void shouldFindTableByIdWhenBelongsToEmpresa() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();
        TableResponseDTO response = TableResponseDTO.builder()
                .id(1L)
                .nombre("Inventario Principal")
                .empresaId(1L)
                .build();

        when(tablaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(tabla));
        when(tableMapper.toResponse(tabla)).thenReturn(response);

        TableResponseDTO result = tableService.findById(1L, usuario);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenTableDoesNotBelongToEmpresa")
    void shouldThrowResourceNotFoundExceptionWhenTableDoesNotBelongToEmpresa() {
        Usuario usuario = crearUsuario();

        when(tablaRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.findById(99L, usuario));
    }

    @Test
    @DisplayName("shouldAddColumnWhenTableExists")
    void shouldAddColumnWhenTableExists() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();

        ColumnRequestDTO request = new ColumnRequestDTO();
        request.setNombre("Stock");
        request.setOrden(1);

        Columna columna = Columna.builder()
                .id(1L)
                .nombre("Stock")
                .orden(1)
                .tabla(tabla)
                .build();

        ColumnResponseDTO response = ColumnResponseDTO.builder()
                .id(1L)
                .nombre("Stock")
                .orden(1)
                .build();

        when(tablaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(tabla));
        when(columnaRepository.save(any(Columna.class))).thenReturn(columna);
        when(tableMapper.toColumnResponse(columna)).thenReturn(response);

        ColumnResponseDTO result = tableService.addColumn(1L, request, usuario);

        assertThat(result.getNombre()).isEqualTo("Stock");
        verify(columnaRepository).save(any(Columna.class));
    }

    @Test
    @DisplayName("shouldAddRowWithoutProductWhenTableExists")
    void shouldAddRowWithoutProductWhenTableExists() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();

        RowRequestDTO request = new RowRequestDTO();
        request.setOrden(1);

        Fila fila = Fila.builder()
                .id(1L)
                .orden(1)
                .tabla(tabla)
                .build();

        RowResponseDTO response = RowResponseDTO.builder()
                .id(1L)
                .orden(1)
                .productoId(null)
                .build();

        when(tablaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(tabla));
        when(filaRepository.save(any(Fila.class))).thenReturn(fila);
        when(tableMapper.toRowResponse(fila)).thenReturn(response);

        RowResponseDTO result = tableService.addRow(1L, request, usuario);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductoId()).isNull();
        verify(productoRepository, never()).findByIdAndEmpresaId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("shouldAddRowWithProductWhenProductExists")
    void shouldAddRowWithProductWhenProductExists() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();

        Producto producto = Producto.builder()
                .id(5L)
                .nombre("Mouse")
                .empresa(usuario.getEmpresa())
                .build();

        RowRequestDTO request = new RowRequestDTO();
        request.setOrden(1);
        request.setProductoId(5L);

        Fila fila = Fila.builder()
                .id(1L)
                .orden(1)
                .tabla(tabla)
                .producto(producto)
                .build();

        RowResponseDTO response = RowResponseDTO.builder()
                .id(1L)
                .orden(1)
                .productoId(5L)
                .build();

        when(tablaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(tabla));
        when(productoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(producto));
        when(filaRepository.save(any(Fila.class))).thenReturn(fila);
        when(tableMapper.toRowResponse(fila)).thenReturn(response);

        RowResponseDTO result = tableService.addRow(1L, request, usuario);

        assertThat(result.getProductoId()).isEqualTo(5L);
        verify(productoRepository).findByIdAndEmpresaId(5L, 1L);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenAddingRowWithMissingProduct")
    void shouldThrowResourceNotFoundExceptionWhenAddingRowWithMissingProduct() {
        Usuario usuario = crearUsuario();
        TablaInventario tabla = crearTabla();

        RowRequestDTO request = new RowRequestDTO();
        request.setOrden(1);
        request.setProductoId(99L);

        when(tablaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(tabla));
        when(productoRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.addRow(1L, request, usuario));

        verify(filaRepository, never()).save(any(Fila.class));
    }

    @Test
    @DisplayName("shouldUpdateCellWhenCellBelongsToUserEmpresa")
    void shouldUpdateCellWhenCellBelongsToUserEmpresa() {
        Usuario usuario = crearUsuario();
        Celda celda = crearCelda(usuario.getEmpresa());

        CellUpdateDTO request = new CellUpdateDTO();
        request.setValorTexto("Nuevo valor");

        CellResponseDTO response = CellResponseDTO.builder()
                .id(1L)
                .columnaId(1L)
                .valorTexto("Nuevo valor")
                .build();

        when(celdaRepository.findById(1L)).thenReturn(Optional.of(celda));
        when(celdaRepository.save(celda)).thenReturn(celda);
        when(tableMapper.toCellResponse(celda)).thenReturn(response);

        CellResponseDTO result = tableService.updateCell(1L, request, usuario);

        assertThat(result.getValorTexto()).isEqualTo("Nuevo valor");
        assertThat(celda.getValorTexto()).isEqualTo("Nuevo valor");
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCellBelongsToAnotherEmpresa")
    void shouldThrowResourceNotFoundExceptionWhenCellBelongsToAnotherEmpresa() {
        Usuario usuario = crearUsuario();

        Empresa otraEmpresa = Empresa.builder()
                .id(2L)
                .nombre("Otra empresa")
                .build();

        Celda celda = crearCelda(otraEmpresa);

        CellUpdateDTO request = new CellUpdateDTO();
        request.setValorTexto("Nuevo valor");

        when(celdaRepository.findById(1L)).thenReturn(Optional.of(celda));

        assertThrows(ResourceNotFoundException.class, () -> tableService.updateCell(1L, request, usuario));

        verify(celdaRepository, never()).save(any(Celda.class));
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

    private TablaInventario crearTabla() {
        return TablaInventario.builder()
                .id(1L)
                .nombre("Inventario Principal")
                .empresa(Empresa.builder().id(1L).nombre("Empresa Test").build())
                .build();
    }

    private Celda crearCelda(Empresa empresa) {
        TablaInventario tabla = TablaInventario.builder()
                .id(1L)
                .nombre("Inventario")
                .empresa(empresa)
                .build();

        Fila fila = Fila.builder()
                .id(1L)
                .tabla(tabla)
                .build();

        Columna columna = Columna.builder()
                .id(1L)
                .nombre("Stock")
                .tabla(tabla)
                .build();

        return Celda.builder()
                .id(1L)
                .fila(fila)
                .columna(columna)
                .valorTexto("Valor anterior")
                .build();
    }
}