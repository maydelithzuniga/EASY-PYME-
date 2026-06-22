package com.easymype.backend.service;

import com.easymype.backend.dto.table.*;
import com.easymype.backend.entity.*;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.mapper.TableMapper;
import com.easymype.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TablaInventarioRepository tablaRepository;
    private final ColumnaRepository columnaRepository;
    private final FilaRepository filaRepository;
    private final CeldaRepository celdaRepository;
    private final ProductoRepository productoRepository;
    private final TableMapper tableMapper;
    private final CategoriaRepository  categoriaRepository;
    private final PlantillaTablaRepository plantillaTablaRepository;
    private final ReglaRelacionService reglaRelacionService;
    @Transactional
    public TableResponseDTO create(TableRequestDTO request, Usuario usuario) {

        TablaInventario tabla = TablaInventario.builder()
                .nombre(request.getNombre())
                .empresa(usuario.getEmpresa())
                .build();

        tablaRepository.save(tabla);
        List<Columna> columnas;

        if (request.getPlantillaId() != null) {
            PlantillaTabla plantilla = plantillaTablaRepository.findById(request.getPlantillaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));

            columnas = plantilla.getColumnas().stream()
                    .map(pc -> Columna.builder()
                            .nombre(pc.getNombre())
                            .orden(pc.getOrden())
                            .tipo(pc.getTipo())
                            .esRequerida(pc.getEsRequerida())
                            .tabla(tabla)
                            .build())
                    .toList();
        } else {
            columnas = List.of(
                    Columna.builder().nombre("Producto").orden(1).tipo(TipoColumna.TEXTO).esRequerida(true).tabla(tabla).build(),
                    Columna.builder().nombre("SKU").orden(2).tipo(TipoColumna.TEXTO).esRequerida(true).tabla(tabla).build(),
                    Columna.builder().nombre("Stock").orden(3).tipo(TipoColumna.STOCK).esRequerida(true).tabla(tabla).build(),
                    Columna.builder().nombre("Precio").orden(4).tipo(TipoColumna.PRECIO).esRequerida(true).tabla(tabla).build()
            );
        }

        columnaRepository.saveAll(columnas);
        tabla.setColumnas(columnas);

        return tableMapper.toResponse(tabla);
    }

    @Transactional(readOnly = true)
    public List<TableResponseDTO> findAll(Usuario usuario) {
        return tableMapper.toResponseList(
                tablaRepository.findByEmpresaId(usuario.getEmpresa().getId())
        );
    }

    @Transactional(readOnly = true)
    public TableResponseDTO findById(Long id, Usuario usuario) {
        TablaInventario tabla = getTablaOrThrow(id, usuario.getEmpresa().getId());
        return tableMapper.toResponse(tabla);
    }

    @Transactional
    public ColumnResponseDTO addColumn(Long tablaId, ColumnRequestDTO request, Usuario usuario) {
        TablaInventario tabla = getTablaOrThrow(tablaId, usuario.getEmpresa().getId());

        Columna columna = Columna.builder()
                .nombre(request.getNombre())
                .orden(request.getOrden())
                .tipo(request.getTipo())
                .esRequerida(request.getEsRequerida())
                .valorDefault(request.getValorDefault())
                .tabla(tabla)
                .build();
        if (request.getTipo() == TipoColumna.CATEGORIA) {
            if (request.getCategoriaIds() == null || request.getCategoriaIds().isEmpty()) {
                throw new IllegalArgumentException(
                        "Una columna de tipo CATEGORIA debe tener al menos una categoría permitida");
            }
            List<Categoria> categorias = categoriaRepository.findAllById(request.getCategoriaIds());
            columna.setCategoriasPermitidas(categorias);
        }
        return tableMapper.toColumnResponse(columnaRepository.save(columna));
    }

    @Transactional
    public RowResponseDTO addRow(Long tablaId, RowRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        TablaInventario tabla = getTablaOrThrow(tablaId, empresaId);

        Fila fila = Fila.builder()
                .orden(request.getOrden())
                .tabla(tabla)
                .build();

        if (request.getProductoId() != null) {
            Producto producto = productoRepository.findByIdAndEmpresaId(request.getProductoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto con id=" + request.getProductoId() + " no encontrado"));
            fila.setProducto(producto);
        }

        return tableMapper.toRowResponse(filaRepository.save(fila));
    }

    private BigDecimal parseNumerico(String valor) {
        try {
            return valor == null ? null : new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    @Transactional
    public CellResponseDTO updateCell(Long cellId, CellUpdateDTO request, Usuario usuario) {
        Celda celda = celdaRepository.findById(cellId)
                .orElseThrow(() -> new ResourceNotFoundException("Celda con id=" + cellId + " no encontrada"));

        Long empresaIdCelda = celda.getFila().getTabla().getEmpresa().getId();
        if (!empresaIdCelda.equals(usuario.getEmpresa().getId())) {
            throw new ResourceNotFoundException("Celda con id=" + cellId + " no encontrada");
        }

        BigDecimal valorAnterior = parseNumerico(celda.getValorTexto());
        celda.setValorTexto(request.getValorTexto());
        celdaRepository.save(celda);
        BigDecimal valorNuevo = parseNumerico(request.getValorTexto());

        if (valorAnterior != null && valorNuevo != null && valorAnterior.compareTo(valorNuevo) != 0) {
            BigDecimal delta = valorNuevo.subtract(valorAnterior);
            reglaRelacionService.procesarTrigger(celda, delta, new HashSet<>()); // set vacío = inicio de cadena
        }

        return tableMapper.toCellResponse(celda);
    }

    private TablaInventario getTablaOrThrow(Long id, Long empresaId) {
        return tablaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tabla con id=" + id + " no encontrada"));
    }
}
