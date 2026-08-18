package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.IngresoExternoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IIngresoExternoRepository extends JpaRepository<IngresoExternoEntity, Long> {
    @Query("""
        SELECT i FROM IngresoExternoEntity i
        LEFT JOIN FETCH i.banco
        LEFT JOIN FETCH i.recibo
        WHERE (:inicio IS NULL OR i.fecha >= :inicio)
          AND (:fin IS NULL OR i.fecha <= :fin)
        ORDER BY i.fecha DESC, i.id DESC
    """)
    List<IngresoExternoEntity> buscar(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
