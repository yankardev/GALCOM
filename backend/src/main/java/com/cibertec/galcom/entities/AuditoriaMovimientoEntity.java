package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "auditoria_movimientos")
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaMovimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(length = 255)
    private String detalle;
}
