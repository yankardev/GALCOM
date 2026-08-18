package com.cibertec.galcom.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoBancario {
    private Long id;
    private Long bancoId;
    private String bancoNombre;
    private Long cuentaId;
    private Long reciboId;
    private String tipo;
    private LocalDate fechaDeposito;
    private String numeroOperacion;
    private BigDecimal monto;
    private String observaciones;
}
