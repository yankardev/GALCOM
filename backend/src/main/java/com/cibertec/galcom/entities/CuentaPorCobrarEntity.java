package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "cuentas_por_cobrar")
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPorCobrarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private ServicioEntity servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio")
    private SocioEntity socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto")
    private PuestoEntity puesto;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(name = "lectura_inicial", precision = 12, scale = 2)
    private BigDecimal lecturaInicial;

    @Column(name = "lectura_final", precision = 12, scale = 2)
    private BigDecimal lecturaFinal;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, length = 15)
    private String estado;

    @Version
    @Column(nullable = false)
    private Long version;
}