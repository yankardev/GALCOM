package com.cibertec.galcom.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private Recibo recibo;
    private BigDecimal totalPagado;
    private List<Long> cuentasAbonadas;
    private List<Long> cuentasExoneradas;
}
