package com.cibertec.galcom.models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanjeBancarioRequest {
    @NotNull(message = "La cuenta es obligatoria")
    private Long cuentaId;

    @NotNull(message = "El banco es obligatorio")
    private Long bancoId;

    @NotNull(message = "La fecha de depósito es obligatoria")
    private LocalDate fechaDeposito;

    private String numeroOperacion;
    private String observaciones;
}
