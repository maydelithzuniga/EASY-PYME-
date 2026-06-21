package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "filas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden")
    private Integer orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabla_id", nullable = false)
    private TablaInventario tabla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @OneToMany(mappedBy = "fila", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Celda> celdas = new ArrayList<>();
}
