package com.cibertec.galcom.repositories;

import com.cibertec.galcom.entities.ReciboEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IReciboRepository extends JpaRepository<ReciboEntity, Long> {
    Optional<ReciboEntity> findTopByOrderByIdDesc();

    @Query("""
        SELECT DISTINCT r FROM ReciboEntity r
        LEFT JOIN FETCH r.usuario
        LEFT JOIN FETCH r.socio
        LEFT JOIN FETCH r.puesto
        LEFT JOIN FETCH r.detalles d
        LEFT JOIN FETCH d.cuenta c
        LEFT JOIN FETCH c.servicio
        WHERE r.id = :id
    """)
    Optional<ReciboEntity> findByIdConRelaciones(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT r FROM ReciboEntity r
        LEFT JOIN FETCH r.usuario
        LEFT JOIN FETCH r.socio
        LEFT JOIN FETCH r.puesto
        WHERE r.fecha >= :inicio AND r.fecha < :fin
        AND (:tipo IS NULL OR r.tipo = :tipo)
        ORDER BY r.fecha DESC
    """)
    List<ReciboEntity> findByFechaTipo(@Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin,
                                      @Param("tipo") String tipo);

    @Query("""
        SELECT DISTINCT r FROM ReciboEntity r
        LEFT JOIN FETCH r.usuario
        LEFT JOIN FETCH r.socio
        LEFT JOIN FETCH r.puesto
        WHERE r.socio.id = :socioId
        ORDER BY r.fecha DESC
    """)
    List<ReciboEntity> findBySocio(@Param("socioId") Long socioId);

    @Query("""
        SELECT DISTINCT r FROM ReciboEntity r
        LEFT JOIN FETCH r.usuario
        LEFT JOIN FETCH r.socio
        LEFT JOIN FETCH r.puesto
        WHERE r.puesto.id = :puestoId
        ORDER BY r.fecha DESC
    """)
    List<ReciboEntity> findByPuesto(@Param("puestoId") Long puestoId);

    List<ReciboEntity> findTop5ByEstadoOrderByFechaDesc(String estado);
}
