package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(unique = true, length = 120)
    private String correo;

    @Column(nullable = false)
    private Boolean estado;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolEntity rol;
}
