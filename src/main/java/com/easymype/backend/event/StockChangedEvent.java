package com.easymype.backend.event;
import com.easymype.backend.entity.Producto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class StockChangedEvent extends ApplicationEvent {
    private final Producto producto;

    public StockChangedEvent(Object source, Producto producto) {
        super(source);
        this.producto = producto;
    }

}
