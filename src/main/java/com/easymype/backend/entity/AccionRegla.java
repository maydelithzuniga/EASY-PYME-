package com.easymype.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "accion_regla")
public class AccionRegla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regla_id")
    private ReglaRelacion regla;

    @ManyToOne
    @JoinColumn(name = "tabla_destino_id")
    private TablaInventario tablaDestino; // Tabla C

    @ManyToOne
    @JoinColumn(name = "columna_destino_id")
    private Columna columnaDestino; // "Total" o "Final"

    // Condición de filas afectadas en destino
    @ManyToOne
    @JoinColumn(name = "columna_condicion_destino_id")
    private Columna columnaCondicionDestino; // "Insumo" (null = TODAS las filas)

    @ElementCollection
    private List<String> valoresCondicionDestino; // ["Agujetas","Cuero","Piezas"]

    @Enumerated(EnumType.STRING)
    private ModoCalculo modoCalculo; // PROPORCIONAL o FIJO

    private BigDecimal factor; // si PROPORCIONAL: se multiplica por el delta del trigger
    // si FIJO: es el valor que se suma/resta siempre
}
