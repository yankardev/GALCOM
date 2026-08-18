package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarCuentasSociosRequest {

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

    @NotEmpty(message = "Debe seleccionar al menos una etapa")
    private List<@Min(value = 1, message = "La etapa mínima es 1")
    @Max(value = 3, message = "La etapa máxima es 3") Integer> etapas;

    private Boolean sociosUnicos;
}