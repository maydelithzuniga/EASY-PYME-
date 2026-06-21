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
            String mensaje = String.format(
                    "El producto '%s' (SKU: %s) está AGOTADO.",
                    producto.getNombre(), producto.getSku());
            AlertaInventario alerta = AlertaInventario.builder()
                    .producto(producto)
                    .empresa(empresa)
                    .tipo(TipoAlerta.STOCK_AGOTADO)
                    .mensaje(mensaje)
                    .leida(false)
                    .build();
            alertaRepository.save(alerta);
            emailService.sendStockAlertEmail(emailDestino, producto.getNombre(),
                    producto.getSku(), TipoAlerta.STOCK_AGOTADO, mensaje);
            log.warn("Alerta STOCK_AGOTADO generada para producto id={}", producto.getId());

        } else if (producto.getEstadoStock() == EstadoStock.BAJO) {
            String mensaje = String.format(
                    "El producto '%s' (SKU: %s) tiene stock bajo: %d unidades (mínimo: %d).",
                    producto.getNombre(), producto.getSku(),
                    producto.getStockActual(), producto.getStockMinimo());
            AlertaInventario alerta = AlertaInventario.builder()
                    .producto(producto)
                    .empresa(empresa)
                    .tipo(TipoAlerta.STOCK_BAJO)
                    .mensaje(mensaje)
                    .leida(false)
                    .build();
            alertaRepository.save(alerta);
            emailService.sendStockAlertEmail(emailDestino, producto.getNombre(),
                    producto.getSku(), TipoAlerta.STOCK_BAJO, mensaje);
            log.warn("Alerta STOCK_BAJO generada para producto id={}", producto.getId());
        }
    }
}
