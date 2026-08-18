package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@Table(name = "recibos")
@NoArgsConstructor
@AllArgsConstructor
public class ReciboEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Long id;

    @Column(name = "numero_correlativo", nullable = false, unique = true, length = 30)
    private String numeroCorrelativo;

    @Column(nullable = false, length = 15)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio")
    private SocioEntity socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto")
    private PuestoEntity puesto;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "metodo_pago", length = 20)
    private String metodoPago;

    @Column(nullable = false, length = 15)
    private String estado;

    @Builder.Default
    @OneToMany(mappedBy = "recibo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReciboEntity> detalles = new ArrayList<>();
}
