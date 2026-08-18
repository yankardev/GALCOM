package com.cibertec.galcom.models;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {
    @Builder.Default
    private List<Long> cuentasAbonar = new ArrayList<>();

    @Builder.Default
    private List<Long> cuentasExonerar = new ArrayList<>();

    private String metodoPago;

    private Long bancoId;
    private LocalDate fechaDeposito;
    private String numeroOperacion;
    private String observaciones;
}
