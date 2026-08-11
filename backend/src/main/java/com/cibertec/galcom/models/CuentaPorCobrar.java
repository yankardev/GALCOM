package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPorCobrar {

    private Long id;

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;
    private String servicioNombre;

    private Long socioId;
    private String socioNombre;

    private Long puestoId;
    private String puestoNumero;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    @PositiveOrZero
    private BigDecimal lecturaInicial;

    @PositiveOrZero
    private BigDecimal lecturaFinal;

    private BigDecimal costoUnitario;

    @PositiveOrZero
    private BigDecimal monto;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @Pattern(regexp = "PENDIENTE|ABONADA|EXONERADA|ANULADA",
            message = "Estado inválido")
    private String estado;
}