package com.cibertec.galcom.models;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenResponsable {
    private String tipo;
    private Long id;
    private String nombre;
    private BigDecimal totalPendiente;
    private List<CuentaPorCobrar> cuentas;
    private List<Recibo> recibos;
}
