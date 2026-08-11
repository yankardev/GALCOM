package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "socios")
@NoArgsConstructor
@AllArgsConstructor
public class SocioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(unique = true, length = 8)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String correo;

    @Column(length = 50)
    private String accion;

    private Integer etapa;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private Boolean estado;
}
