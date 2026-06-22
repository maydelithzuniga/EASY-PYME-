package com.easymype.backend.event;

import com.easymype.backend.entity.*;
import com.easymype.backend.repository.AlertaInventarioRepository;
import com.easymype.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAlertListener {

    private final AlertaInventarioRepository alertaRepository;
    private final EmailService emailService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSaleCreated(SaleCreatedEvent event) {
        Venta venta = event.getVenta();
        log.info("Procesando alertas de stock para venta id={}", venta.getId());

        String emailDestino = venta.getUsuario().getEmail();
        venta.getDetalles().forEach(detalle -> {
            Producto producto = detalle.getProducto();
            generarAlertaSiNecesario(producto, venta.getEmpresa(), emailDestino);
        });
    }

    private void generarAlertaSiNecesario(Producto producto, Empresa empresa, String emailDestino) {
        if (producto.getEstadoStock() == EstadoStock.AGOTADO) {
            generarYEnviarAlerta(producto, empresa, emailDestino, TipoAlerta.STOCK_AGOTADO,
                    String.format("El producto '%s' (SKU: %s) está AGOTADO.",
                            producto.getNombre(), producto.getSku()));

        } else if (producto.getEstadoStock() == EstadoStock.BAJO) {
            generarYEnviarAlerta(producto, empresa, emailDestino, TipoAlerta.STOCK_BAJO,
                    String.format("El producto '%s' (SKU: %s) tiene stock bajo: %d unidades (mínimo: %d).",
                            producto.getNombre(), producto.getSku(),
                            producto.getStockActual(), producto.getStockMinimo()));
        }
    }

    private void generarYEnviarAlerta(Producto producto, Empresa empresa, String emailDestino,
                                      TipoAlerta tipo, String mensaje) {
        boolean yaExisteAlertaActiva = alertaRepository
                .existsByProductoIdAndTipoAndLeidaFalse(producto.getId(), tipo);

        if (yaExisteAlertaActiva) {
            log.info("Alerta {} ya existe y está sin leer para producto id={}, se omite duplicado",
                    tipo, producto.getId());
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

        emailService.sendStockAlertEmail(emailDestino, producto.getNombre(),
                producto.getSku(), tipo, mensaje);

        log.warn("Alerta {} generada para producto id={}", tipo, producto.getId());
    }
}
