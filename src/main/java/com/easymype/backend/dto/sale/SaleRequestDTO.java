package com.easymype.backend.dto.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaleRequestDTO {

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<SaleItemRequestDTO> items;
}
