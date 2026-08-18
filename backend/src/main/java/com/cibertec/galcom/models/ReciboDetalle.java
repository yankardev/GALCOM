package com.cibertec.galcom.models;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReciboDetalle {
    private Long cuentaId;
    private String servicio;
    private String periodo;
    private BigDecimal montoAplicado;
}
