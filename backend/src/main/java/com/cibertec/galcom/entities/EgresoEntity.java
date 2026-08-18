package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "egresos")
@NoArgsConstructor
@AllArgsConstructor
public class EgresoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_egreso")
    private Long id;

    @Column(name = "tipo_documento", length = 30)
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(nullable = false, length = 150)
    private String proveedor;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal impuesto;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "documento_asociado", length = 100)
    private String documentoAsociado;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(name = "archivo_origen", length = 255)
    private String archivoOrigen;

    @Column(nullable = false, length = 15)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco")
    private BancoEntity banco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibo")
    private ReciboEntity recibo;
}
