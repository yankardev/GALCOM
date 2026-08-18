package com.cibertec.galcom.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "secuencia_recibos")
@NoArgsConstructor
@AllArgsConstructor
public class SecuenciaReciboEntity {
    @Id
    private Integer id;

    @Column(nullable = false)
    private Long ultimo;
}
