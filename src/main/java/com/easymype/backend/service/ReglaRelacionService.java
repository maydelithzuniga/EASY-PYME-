package com.easymype.backend.service;

import com.easymype.backend.dto.ReglaRelacion.AccionReglaRequestDTO;
import com.easymype.backend.dto.ReglaRelacion.AccionReglaResponseDTO;
import com.easymype.backend.dto.ReglaRelacion.ReglaRelacionRequestDTO;
import com.easymype.backend.dto.ReglaRelacion.ReglaRelacionResponseDTO;
import com.easymype.backend.entity.*;
import com.easymype.backend.event.StockChangedEvent;
import com.easymype.backend.exception.ResourceNotFoundException;
import com.easymype.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReglaRelacionService {
    private final ReglaRelacionRepository reglaRepository;
    private final FilaRepository filaRepository;
    private final CeldaRepository celdaRepository;
    private final TablaInventarioRepository tablaRepository;
    private final ColumnaRepository columnaRepository;
    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int MAX_PROFUNDIDAD = 5; // protección contra cadenas largas/cíclicas

    @Transactional
    public void procesarTrigger(Celda celdaOrigen, BigDecimal delta, Set<Long> tablasVisitadas) {
        Long tablaOrigenId = celdaOrigen.getFila().getTabla().getId();

        // Guard de ciclos: si ya pasamos por esta tabla en esta cadena, cortamos
        if (tablasVisitadas.contains(tablaOrigenId) || tablasVisitadas.size() >= MAX_PROFUNDIDAD) {
            return;
        }
        tablasVisitadas.add(tablaOrigenId);

        Long columnaId = celdaOrigen.getColumna().getId();
        List<ReglaRelacion> reglas = reglaRepository
                .findByTablaOrigenIdAndColumnaTriggerIdAndActivaTrue(tablaOrigenId, columnaId);

        for (ReglaRelacion regla : reglas) {
            if (!disparaSegunTipo(regla.getTipoDisparo(), delta)) continue;
            if (!cumpleCondicionFila(celdaOrigen.getFila(), regla)) continue;

            for (AccionRegla accion : regla.getAcciones()) {
                aplicarAccion(accion, delta, tablasVisitadas);
            }
        }
    }

    private boolean disparaSegunTipo(TipoDisparo tipo, BigDecimal delta) {
        return switch (tipo) {
            case DECREMENTO -> delta.compareTo(BigDecimal.ZERO) < 0;
            case INCREMENTO -> delta.compareTo(BigDecimal.ZERO) > 0;
            case CUALQUIER_CAMBIO -> true;
        };
    }

    private boolean cumpleCondicionFila(Fila fila, ReglaRelacion regla) {
        if (regla.getColumnaCondicionOrigen() == null) return true; // aplica a cualquier fila

        return fila.getCeldas().stream()
                .filter(c -> c.getColumna().getId().equals(regla.getColumnaCondicionOrigen().getId()))
                .anyMatch(c -> regla.getValorCondicionOrigen().equalsIgnoreCase(c.getValorTexto()));
    }

    private void aplicarAccion(AccionRegla accion, BigDecimal deltaOrigen, Set<Long> tablasVisitadas) {

        List<Fila> filasDestino = obtenerFilasDestino(accion);

        BigDecimal cambio = accion.getModoCalculo() == ModoCalculo.PROPORCIONAL
                ? deltaOrigen.multiply(accion.getFactor())
                : accion.getFactor();

        for (Fila fila : filasDestino) {
            Celda celdaDestino = obtenerOCrearCelda(fila, accion.getColumnaDestino());

            BigDecimal valorActual = parseNumerico(celdaDestino.getValorTexto());
            valorActual = valorActual != null ? valorActual : BigDecimal.ZERO;
            BigDecimal nuevoValor = valorActual.add(cambio);

            celdaDestino.setValorTexto(nuevoValor.toString());
            celdaRepository.save(celdaDestino);

            // NUEVO: si la celda es de tipo STOCK, sincroniza Producto y publica evento
            if (celdaDestino.getColumna().getTipo() == TipoColumna.STOCK
                    && celdaDestino.getFila().getProducto() != null) {
                Producto producto = celdaDestino.getFila().getProducto();
                producto.setStockActual(nuevoValor.intValue());
                producto.recalcularEstadoStock();
                productoRepository.save(producto);
                eventPublisher.publishEvent(new StockChangedEvent(this, producto));
            }

            procesarTrigger(celdaDestino, cambio, new HashSet<>(tablasVisitadas));
        }
    }

    private List<Fila> obtenerFilasDestino(AccionRegla accion) {
        List<Fila> todasLasFilas = filaRepository.findByTablaId(accion.getTablaDestino().getId());

        if (accion.getColumnaCondicionDestino() == null) {
            return todasLasFilas; // caso "Final" → todas las filas
        }

        return todasLasFilas.stream()
                .filter(fila -> fila.getCeldas().stream()
                        .filter(c -> c.getColumna().getId().equals(accion.getColumnaCondicionDestino().getId()))
                        .anyMatch(c -> accion.getValoresCondicionDestino().contains(c.getValorTexto())))
                .toList();
    }

    private Celda obtenerOCrearCelda(Fila fila, Columna columna) {
        return fila.getCeldas().stream()
                .filter(c -> c.getColumna().getId().equals(columna.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Celda nueva = Celda.builder().fila(fila).columna(columna).valorTexto("0").build();
                    return celdaRepository.save(nueva);
                });
    }

    private BigDecimal parseNumerico(String valor) {
        try {
            return valor == null ? null : new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Transactional
    public ReglaRelacionResponseDTO create(ReglaRelacionRequestDTO request, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        if (request.getAcciones() == null || request.getAcciones().isEmpty()) {
            throw new IllegalArgumentException("La regla debe tener al menos una acción");
        }

        TablaInventario tablaOrigen = tablaRepository
                .findByIdAndEmpresaId(request.getTablaOrigenId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tabla origen con id=" + request.getTablaOrigenId() + " no encontrada"));

        Columna columnaTrigger = obtenerColumnaDeTabla(request.getColumnaTriggerId(), tablaOrigen.getId());

        Columna columnaCondicionOrigen = null;
        if (request.getColumnaCondicionOrigenId() != null) {
            columnaCondicionOrigen = obtenerColumnaDeTabla(request.getColumnaCondicionOrigenId(), tablaOrigen.getId());
            if (request.getValorCondicionOrigen() == null || request.getValorCondicionOrigen().isBlank()) {
                throw new IllegalArgumentException(
                        "Debe especificar valorCondicionOrigen cuando se define columnaCondicionOrigenId");
            }
        }

        ReglaRelacion regla = ReglaRelacion.builder()
                .nombre(request.getNombre())
                .tablaOrigen(tablaOrigen)
                .columnaTrigger(columnaTrigger)
                .columnaCondicionOrigen(columnaCondicionOrigen)
                .valorCondicionOrigen(request.getValorCondicionOrigen())
                .tipoDisparo(request.getTipoDisparo())
                .activa(true)
                .build();

        List<AccionRegla> acciones = request.getAcciones().stream()
                .map(accionReq -> construirAccion(accionReq, regla, empresaId))
                .toList();

        regla.setAcciones(acciones);

        ReglaRelacion guardada = reglaRepository.save(regla);
        return mapearAResponse(guardada);
    }

    private AccionRegla construirAccion(AccionReglaRequestDTO accionReq, ReglaRelacion regla, Long empresaId) {
        if (accionReq.getModoCalculo() == null) {
            throw new IllegalArgumentException("modoCalculo es obligatorio en cada acción");
        }
        if (accionReq.getFactor() == null) {
            throw new IllegalArgumentException("factor es obligatorio en cada acción");
        }

        TablaInventario tablaDestino = tablaRepository
                .findByIdAndEmpresaId(accionReq.getTablaDestinoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tabla destino con id=" + accionReq.getTablaDestinoId() + " no encontrada"));

        Columna columnaDestino = obtenerColumnaDeTabla(accionReq.getColumnaDestinoId(), tablaDestino.getId());

        Columna columnaCondicionDestino = null;
        if (accionReq.getColumnaCondicionDestinoId() != null) {
            columnaCondicionDestino = obtenerColumnaDeTabla(
                    accionReq.getColumnaCondicionDestinoId(), tablaDestino.getId());
            if (accionReq.getValoresCondicionDestino() == null || accionReq.getValoresCondicionDestino().isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe especificar valoresCondicionDestino cuando se define columnaCondicionDestinoId");
            }
        }

        return AccionRegla.builder()
                .regla(regla)
                .tablaDestino(tablaDestino)
                .columnaDestino(columnaDestino)
                .columnaCondicionDestino(columnaCondicionDestino)
                .valoresCondicionDestino(accionReq.getValoresCondicionDestino())
                .modoCalculo(accionReq.getModoCalculo())
                .factor(accionReq.getFactor())
                .build();
    }

    private Columna obtenerColumnaDeTabla(Long columnaId, Long tablaId) {
        Columna columna = columnaRepository.findById(columnaId)
                .orElseThrow(() -> new ResourceNotFoundException("Columna con id=" + columnaId + " no encontrada"));

        if (!columna.getTabla().getId().equals(tablaId)) {
            throw new IllegalArgumentException(
                    "La columna con id=" + columnaId + " no pertenece a la tabla con id=" + tablaId);
        }
        return columna;
    }

    private ReglaRelacionResponseDTO mapearAResponse(ReglaRelacion regla) {
        return ReglaRelacionResponseDTO.builder()
                .id(regla.getId())
                .nombre(regla.getNombre())
                .tablaOrigenId(regla.getTablaOrigen().getId())
                .tablaOrigenNombre(regla.getTablaOrigen().getNombre())
                .columnaTriggerId(regla.getColumnaTrigger().getId())
                .columnaTriggerNombre(regla.getColumnaTrigger().getNombre())
                .tipoDisparo(regla.getTipoDisparo())
                .activa(regla.isActiva())
                .acciones(regla.getAcciones().stream()
                        .map(this::mapearAccionAResponse)
                        .toList())
                .build();
    }

    private AccionReglaResponseDTO mapearAccionAResponse(AccionRegla accion) {
        return AccionReglaResponseDTO.builder()
                .id(accion.getId())
                .tablaDestinoId(accion.getTablaDestino().getId())
                .tablaDestinoNombre(accion.getTablaDestino().getNombre())
                .columnaDestinoId(accion.getColumnaDestino().getId())
                .columnaDestinoNombre(accion.getColumnaDestino().getNombre())
                .columnaCondicionDestinoId(
                        accion.getColumnaCondicionDestino() != null ? accion.getColumnaCondicionDestino().getId() : null)
                .columnaCondicionDestinoNombre(
                        accion.getColumnaCondicionDestino() != null ? accion.getColumnaCondicionDestino().getNombre() : null)
                .valoresCondicionDestino(accion.getValoresCondicionDestino())
                .modoCalculo(accion.getModoCalculo())
                .factor(accion.getFactor())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReglaRelacionResponseDTO> findAllByEmpresa(Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();

        return reglaRepository.findByTablaOrigen_Empresa_Id(empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReglaRelacionResponseDTO findById(Long id, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        ReglaRelacion regla = reglaRepository.findByIdAndTablaOrigen_Empresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla con id=" + id + " no encontrada"));

        return mapearAResponse(regla);
    }

    public List<ReglaRelacionResponseDTO> findByTablaOrigen(Long tablaId, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        return reglaRepository.findByTableIdAndTablaOrigen_Empresa_Id(tablaId,empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    public ReglaRelacionResponseDTO cambiarEstado(Long id, boolean b, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        ReglaRelacion regla = reglaRepository.findByIdAndTablaOrigen_Empresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla con id=" + id + " no encontrada"));
        regla.setActiva(b);
        ReglaRelacion actualizada = reglaRepository.save(regla);
        return mapearAResponse(actualizada);
    }
    @Transactional
    public void delete(Long id, Usuario usuario) {
        Long empresaId = usuario.getEmpresa().getId();
        ReglaRelacion regla = reglaRepository.findByIdAndTablaOrigen_Empresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla con id=" + id + " no encontrada"));
        reglaRepository.delete(regla);
    }
}
