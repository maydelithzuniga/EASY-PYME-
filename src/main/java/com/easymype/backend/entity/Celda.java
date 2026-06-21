package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "celdas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Celda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fila_id", nullable = false)
    private Fila fila;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "columna_id", nullable = false)
    private Columna columna;

    @Column(name = "valor_texto")
    private String valorTexto;

    @Column(name = "valor_numero", precision = 12, scale = 2)
    private BigDecimal valorNumero;

    @Column(name = "valor_fecha")
    private LocalDate valorFecha;

    @Column(name = "valor_boolean")
    private Boolean valorBoolean;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria valorCategoria;
}
