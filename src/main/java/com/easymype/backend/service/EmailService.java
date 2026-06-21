package com.easymype.backend.service;

import com.easymype.backend.entity.TipoAlerta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async("taskExecutor")
    public void sendWelcomeEmail(String to, String firstName, String empresaNombre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Bienvenido a EasyMype");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                    "Tu cuenta ha sido creada exitosamente y tu empresa '%s' ya está registrada en EasyMype.\n\n" +
                    "Puedes comenzar a gestionar tu inventario de inmediato.\n\n" +
                    "El equipo de EasyMype",
                    firstName, empresaNombre
            ));
            mailSender.send(message);
            log.info("Email de bienvenida enviado a {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a {}: {}", to, e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendStockAlertEmail(String to, String productName, String sku,
                                    TipoAlerta tipo, String mensaje) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(String.format("[EasyMype] Alerta de inventario: %s", productName));
            message.setText(String.format(
                    "Alerta de inventario\n\n" +
                    "Tipo: %s\n" +
                    "Producto: %s (SKU: %s)\n\n" +
                    "%s\n\n" +
                    "Ingresa a EasyMype para tomar acción.\n\n" +
                    "El equipo de EasyMype",
                    tipo.name(), productName, sku, mensaje
            ));
            mailSender.send(message);
            log.info("Email de alerta de stock enviado a {} para producto {}", to, sku);
        } catch (Exception e) {
            log.error("Error al enviar email de alerta a {}: {}", to, e.getMessage());
        }
    }
}
