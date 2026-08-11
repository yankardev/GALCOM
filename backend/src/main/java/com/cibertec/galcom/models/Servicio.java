package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {
    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    @NotBlank(message = "La recurrencia es obligatoria")
    @Pattern(regexp = "MENSUAL|TRIMESTRAL|ANUAL|EVENTUAL|UNICO")
    private String recurrencia;

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo debe ser cero o positivo")
    private BigDecimal costo;

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "PEN|USD")
    private String moneda;

    @NotBlank(message = "Debe indicar a quién se carga el servicio")
    @Pattern(regexp = "PUESTO|SOCIO")
    private String cargoA;

    @NotBlank(message = "Debe indicar el tipo de cálculo")
    @Pattern(regexp = "FIJO|CONSUMO")
    private String tipoCalculo;

    private Boolean estado;
}
