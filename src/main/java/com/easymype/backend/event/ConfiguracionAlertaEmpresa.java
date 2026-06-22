package com.easymype.backend.event;

import com.easymype.backend.entity.Empresa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_alerta_empresa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionAlertaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String email; // un registro por destinatario

    private boolean recibeAlertas = true;
}
