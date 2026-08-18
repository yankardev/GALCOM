package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "movimientos_bancarios")
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoBancarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", nullable = false)
    private BancoEntity banco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta")
    private CuentaPorCobrarEntity cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibo")
    private ReciboEntity recibo;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "fecha_deposito", nullable = false)
    private LocalDate fechaDeposito;

    @Column(name = "numero_operacion", length = 80)
    private String numeroOperacion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(length = 255)
    private String observaciones;
}
