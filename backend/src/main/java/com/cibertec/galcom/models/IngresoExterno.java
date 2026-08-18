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
public class IngresoExterno {
    private Long id;

    @NotBlank(message = "El depositante es obligatorio")
    private String depositante;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private String observaciones;
    private Long bancoId;
    private String bancoNombre;
    private Long reciboId;
    private String numeroRecibo;
}
