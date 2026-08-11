package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "puestos")
@NoArgsConstructor
@AllArgsConstructor
public class PuestoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(length = 150)
    private String ubicacion;

    @Column(name = "inquilino_nombre", length = 150)
    private String inquilinoNombre;

    @Column(name = "inquilino_documento", length = 20)
    private String inquilinoDocumento;

    @Column(name = "inquilino_telefono", length = 20)
    private String inquilinoTelefono;

    @Column(name = "vigencia_inicio")
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fin")
    private LocalDate vigenciaFin;

    @Column(nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_socio")
    private SocioEntity socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_giro", nullable = false)
    private GiroEntity giro;
}
