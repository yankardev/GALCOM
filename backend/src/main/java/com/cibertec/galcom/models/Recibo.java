package com.cibertec.galcom.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recibo {
    private Long id;
    private String numeroCorrelativo;
    private String tipo;
    private String usuario;
    private Long socioId;
    private String socioNombre;
    private Long puestoId;
    private String puestoNumero;
    private LocalDateTime fecha;
    private BigDecimal montoTotal;
    private String metodoPago;
    private String estado;
    private List<ReciboDetalle> detalles;
}
