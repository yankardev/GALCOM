package com.cibertec.galcom.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
