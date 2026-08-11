package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "giros")
@NoArgsConstructor
@AllArgsConstructor
public class GiroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giro")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado;
}
