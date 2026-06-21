package com.easymype.backend.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Stock insuficiente para '%s': solicitado %d, disponible %d",
                productName, requested, available
        ));
    }
}
