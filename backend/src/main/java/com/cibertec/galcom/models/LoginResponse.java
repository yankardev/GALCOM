package com.cibertec.galcom.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String usuario;
    private String nombres;
    private String apellidos;
    private String rol;
}
