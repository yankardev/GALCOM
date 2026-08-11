package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class RolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;
}
