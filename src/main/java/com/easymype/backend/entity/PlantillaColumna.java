package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plantillas_columna")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaColumna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoColumna tipo;

    private Integer orden;

    private Boolean esRequerida;

    private String valorDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private PlantillaTabla plantilla;
}
