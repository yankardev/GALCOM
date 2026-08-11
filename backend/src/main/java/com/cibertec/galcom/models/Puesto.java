package com.cibertec.galcom.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Puesto {
    private Long id;

    @NotBlank(message = "El número del puesto es obligatorio")
    @Size(max = 20)
    private String numero;

    @Size(max = 150)
    private String ubicacion;

    @Size(max = 150)
    private String inquilinoNombre;

    @Size(max = 20)
    private String inquilinoDocumento;

    @Size(max = 20)
    private String inquilinoTelefono;

    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "DISPONIBLE|OCUPADO|MANTENIMIENTO|INACTIVO")
    private String estado;

    private Long socioId;
    private String socioNombre;

    @NotNull(message = "El giro comercial es obligatorio")
    private Long giroId;
    private String giroNombre;
}
