package com.easymype.backend.dto.dashboard;

import com.easymype.backend.entity.EstadoStock;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProductoAlertaDTO {

    private Long productoId;
    private String nombre;
    private String sku;
    private Integer stockActual;
    private Integer stockMinimo;
    private EstadoStock estadoStock;

}
