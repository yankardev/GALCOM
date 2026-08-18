package com.cibertec.galcom.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumen {
    private long socios;
    private long puestos;
    private long puestosOcupados;
    private long cuentasPendientes;
    private BigDecimal porCobrar;
    private BigDecimal recaudadoMes;
    private BigDecimal egresosMes;
    private List<Recibo> ultimosRecibos;
}
