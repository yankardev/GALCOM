package com.cibertec.galcom.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Giro {
    private Long id;

    @NotBlank(message = "El nombre del giro es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    private Boolean estado;
}
