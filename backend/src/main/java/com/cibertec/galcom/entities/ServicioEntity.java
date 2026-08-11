package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Entity
@Table(name = "servicios")
@NoArgsConstructor
@AllArgsConstructor
public class ServicioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String recurrencia;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "cargo_a", nullable = false, length = 10)
    private String cargoA;

    @Column(name = "tipo_calculo", nullable = false, length = 10)
    private String tipoCalculo;

    @Column(nullable = false)
    private Boolean estado;
}
