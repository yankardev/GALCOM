package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banco {
    private Long id;

    @NotBlank(message = "El nombre del banco es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 50)
    private String numeroCuenta;

    @Size(max = 30)
    private String cci;

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "PEN|USD", message = "La moneda debe ser PEN o USD")
    private String moneda;

    @Size(max = 30)
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    private Boolean estado;
}
