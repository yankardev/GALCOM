package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.EgresoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IEgresoRepository extends JpaRepository<EgresoEntity, Long> {

    @Query("""
        SELECT e FROM EgresoEntity e
        LEFT JOIN FETCH e.banco
        LEFT JOIN FETCH e.recibo
        WHERE (:inicio IS NULL OR e.fecha >= :inicio)
          AND (:fin IS NULL OR e.fecha <= :fin)
        ORDER BY e.fecha DESC, e.id DESC
    """)
    List<EgresoEntity> buscar(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );

    boolean existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCaseAndProveedorIgnoreCase(
            String tipoDocumento,
            String numeroDocumento,
            String proveedor
    );
}