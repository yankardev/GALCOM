package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.CuentaPorCobrarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICuentaPorCobrarRepository extends JpaRepository<CuentaPorCobrarEntity, Long> {
    @Query("""
        SELECT c FROM CuentaPorCobrarEntity c JOIN FETCH c.servicio
        LEFT JOIN FETCH c.socio LEFT JOIN FETCH c.puesto
    """)
    List<CuentaPorCobrarEntity> findAllConRelaciones();
    @Query("""
        SELECT c
        FROM CuentaPorCobrarEntity c JOIN FETCH c.servicio
        LEFT JOIN FETCH c.socio LEFT JOIN FETCH c.puesto
        WHERE c.id = :id
    """)
    Optional<CuentaPorCobrarEntity> findByIdConRelaciones(@Param("id") Long id);
    @Query("""
        SELECT COUNT(c) > 0 FROM CuentaPorCobrarEntity c
        WHERE c.servicio.id = :servicioId AND c.puesto.id = :puestoId AND c.periodo = :periodo
    """)
    boolean existeCuentaPuestoPeriodo(@Param("servicioId") Long servicioId, @Param("puestoId") Long puestoId, @Param("periodo") String periodo);

    @Query("""
            SELECT COUNT(c) > 0 FROM CuentaPorCobrarEntity c WHERE c.servicio.id = :servicioId
            AND c.socio.id = :socioId AND c.periodo = :periodo
            """)
    boolean existeCuentaSocioPeriodo(@Param("servicioId") Long servicioId, @Param("socioId") Long socioId, @Param("periodo") String periodo);
}