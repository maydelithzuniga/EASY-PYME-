package com.easymype.backend.event;

import com.easymype.backend.entity.Venta;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SaleCreatedEvent extends ApplicationEvent {

    private final Venta venta;

    public SaleCreatedEvent(Object source, Venta venta) {
        super(source);
        this.venta = venta;
    }
}
