package com.easymype.backend.event;

import com.easymype.backend.entity.*;
import com.easymype.backend.repository.AlertaInventarioRepository;
import com.easymype.backend.repository.ConfiguracionAlertaEmpresaRepository;
import com.easymype.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAlertListener {

    private final AlertaInventarioRepository alertaRepository;
    private final ConfiguracionAlertaEmpresaRepository configRepository;
    private final EmailService emailService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSaleCreated(SaleCreatedEvent event) {
        Venta venta = event.getVenta();
        venta.getDetalles().forEach(detalle ->
                procesarProducto(detalle.getProducto(), venta.getEmpresa())
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockChanged(StockChangedEvent event) {
        Producto producto = event.getProducto();
        procesarProducto(producto, producto.getEmpresa());
    }

    private void procesarProducto(Producto producto, Empresa empresa) {
        EstadoStock estado = producto.getEstadoStock();

        // --- FIX ERROR 6: transición a NORMAL ---
        if (estado == EstadoStock.DISPONIBLE) {
            resolverAlertasActivas(producto);
            return;
        }

        // --- FIX ERROR 5: destinatarios configurables ---
        List<String> destinatarios = obtenerDestinatarios(empresa);
        if (destinatarios.isEmpty()) {
            log.warn("No hay destinatarios configurados para empresa id={}", empresa.getId());
            return;
        }

        if (estado == EstadoStock.AGOTADO) {
            generarYEnviarAlerta(producto, empresa, destinatarios, TipoAlerta.STOCK_AGOTADO,
                    String.format("El producto '%s' (SKU: %s) está AGOTADO.",
                            producto.getNombre(), producto.getSku()));

        } else if (estado == EstadoStock.BAJO) {
            generarYEnviarAlerta(producto, empresa, destinatarios, TipoAlerta.STOCK_BAJO,
                    String.format("El producto '%s' (SKU: %s) tiene stock bajo: %d unidades (mínimo: %d).",
                            producto.getNombre(), producto.getSku(),
                            producto.getStockActual(), producto.getStockMinimo()));
        }
    }

    // FIX ERROR 5: lee los destinatarios de la tabla de configuración
    private List<String> obtenerDestinatarios(Empresa empresa) {
        return configRepository.findByEmpresaIdAndRecibeAlertasTrue(empresa.getId())
                .stream()
                .map(ConfiguracionAlertaEmpresa::getEmail)
                .collect(Collectors.toList());
    }

    // FIX ERROR 6: cierra las alertas abiertas cuando el stock vuelve a NORMAL
    private void resolverAlertasActivas(Producto producto) {
        List<AlertaInventario> alertasAbiertas = alertaRepository
                .findByProductoIdAndLeidaFalse(producto.getId());

        if (alertasAbiertas.isEmpty()) return;

        alertasAbiertas.forEach(a -> a.setLeida(true));
        alertaRepository.saveAll(alertasAbiertas);
        log.info("Auto-resueltas {} alerta(s) para producto id={} (stock volvió a NORMAL)",
                alertasAbiertas.size(), producto.getId());
    }

    private void generarYEnviarAlerta(Producto producto, Empresa empresa,
                                      List<String> destinatarios,
                                      TipoAlerta tipo, String mensaje) {
        boolean yaExiste = alertaRepository
                .existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), tipo);

        if (yaExiste) {
            log.info("Alerta {} ya activa para producto id={}, omitiendo duplicado", tipo, producto.getId());
            return;
        }

        AlertaInventario alerta = AlertaInventario.builder()
                .producto(producto)
                .empresa(empresa)
                .tipo(tipo)
                .mensaje(mensaje)
                .leida(false)
                .build();
        alertaRepository.save(alerta);

        // Envía a todos los destinatarios configurados
        destinatarios.forEach(email ->
                emailService.sendStockAlertEmail(email, producto.getNombre(),
                        producto.getSku(), tipo, mensaje)
        );

        log.warn("Alerta {} generada para producto id={}, enviada a {} destinatario(s)",
                tipo, producto.getId(), destinatarios.size());
    }
}
