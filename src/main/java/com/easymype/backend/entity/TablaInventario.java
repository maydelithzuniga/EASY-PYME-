package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tablas_inventario")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TablaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @OneToMany(mappedBy = "tabla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Columna> columnas = new ArrayList<>();

    @OneToMany(mappedBy = "tabla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Fila> filas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
