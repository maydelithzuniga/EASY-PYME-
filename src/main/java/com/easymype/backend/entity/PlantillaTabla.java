package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plantillas_tabla")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaTabla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // "Productos", "Insumos", "Activos", "Servicios"

    private String descripcion;

    private String icono;

    private String categoria; // "E-commerce", "Manufacturado", etc.

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlantillaColumna> columnas = new ArrayList<>();
}
