package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Entity
@Table(name = "bancos")
@NoArgsConstructor
@AllArgsConstructor
public class BancoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_banco")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 50)
    private String numeroCuenta;

    @Column(unique = true, length = 30)
    private String cci;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "tipo_cuenta", length = 30)
    private String tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoInicial;

    @Column(nullable = false)
    private Boolean estado;
}
