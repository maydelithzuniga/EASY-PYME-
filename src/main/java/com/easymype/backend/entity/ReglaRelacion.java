package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "regla_relacion")
public class ReglaRelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // "Venta de zapato consume insumos"

    @ManyToOne
    @JoinColumn(name = "tabla_origen_id")
    private TablaInventario tablaOrigen; // Tabla B

    @ManyToOne
    @JoinColumn(name = "columna_trigger_id")
    private Columna columnaTrigger; // "Cantidad" en tabla B

    @ManyToOne
    @JoinColumn(name = "columna_condicion_id")
    private Columna columnaCondicionOrigen; // "Producto" en tabla B (null = cualquier fila)

    private String valorCondicionOrigen; // "Zapato"

    @Enumerated(EnumType.STRING)
    private TipoDisparo tipoDisparo; // DECREMENTO, INCREMENTO, CUALQUIER_CAMBIO

    private boolean activa;

    @OneToMany(mappedBy = "regla", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccionRegla> acciones;
}
