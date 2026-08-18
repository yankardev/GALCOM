package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Entity
@Table(name = "detalle_recibo", uniqueConstraints = @UniqueConstraint(columnNames = {"id_recibo", "id_cuenta"}))
@NoArgsConstructor
@AllArgsConstructor
public class DetalleReciboEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibo", nullable = false)
    private ReciboEntity recibo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaPorCobrarEntity cuenta;

    @Column(name = "monto_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAplicado;
}
