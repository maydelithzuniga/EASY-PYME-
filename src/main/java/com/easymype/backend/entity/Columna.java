package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "columnas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Columna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "orden")
    private Integer orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabla_id", nullable = false)
    private TablaInventario tabla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoColumna tipo = TipoColumna.TEXTO;

    private Boolean esRequerida;

    private String valorDefault;

    @ManyToMany
    @JoinTable(
            name = "columna_categorias_permitidas",
            joinColumns = @JoinColumn(name = "columna_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private List<Categoria> categoriasPermitidas = new ArrayList<>();

    @OneToMany(mappedBy = "columna", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Celda> celdas = new ArrayList<>();
}
