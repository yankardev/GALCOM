package com.cibertec.galcom.models;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Socio {
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20)
    private String codigo;

    @Pattern(regexp = "^$|^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String apellidos;

    @Size(max = 20)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @Size(max = 50)
    private String accion;

    @Min(value = 1, message = "La etapa mínima es 1")
    @Max(value = 3, message = "La etapa máxima es 3")
    private Integer etapa;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    private Boolean estado;
}
