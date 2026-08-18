package com.cibertec.galcom.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarCuentasPuestosRequest {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    @NotBlank(message = "El periodo es obligatorio")
    @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$", message = "El periodo debe tener formato AAAA-MM")
    private String periodo;

    @PositiveOrZero(message = "El monto no puede ser negativo")
    private BigDecimal monto;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;
}